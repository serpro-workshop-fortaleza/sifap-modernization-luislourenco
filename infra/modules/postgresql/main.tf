terraform {
  required_version = ">= 1.9.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

# A senha nunca aparece em locals, variavel de ambiente ou saida do plano.
# Ela e gerada aqui e persistida diretamente no Key Vault.
resource "random_password" "administrator" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "azurerm_postgresql_flexible_server" "main" {
  #checkov:skip=CKV2_AZURE_57:Acesso privado via delegated_subnet_id quando allowed_subnet_id e informado. O recorte 001 nao provisiona VNet. Issue #8.
  name                = "psql-${var.name_prefix}"
  resource_group_name = var.resource_group_name
  location            = var.location

  version    = "16"
  sku_name   = var.sku_name
  storage_mb = var.storage_mb

  administrator_login    = var.administrator_login
  administrator_password = random_password.administrator.result

  backup_retention_days        = var.backup_retention_days
  geo_redundant_backup_enabled = var.geo_redundant_backup

  # Com subnet informada o servidor opera em acesso privado (VNet integrada).
  # Sem subnet, o acesso publico fica habilitado mas SEM nenhuma regra de firewall,
  # o que nega todo trafego ate alguem declarar a origem explicitamente.
  delegated_subnet_id           = var.allowed_subnet_id
  public_network_access_enabled = var.allowed_subnet_id == null

  authentication {
    # Managed Identity para servico a servico; o login local existe apenas para
    # migracao Flyway e operacao de emergencia.
    active_directory_auth_enabled = true
    password_auth_enabled         = true
    tenant_id                     = data.azurerm_client_config.current.tenant_id
  }

  maintenance_window {
    # Janela fora do primeiro dia util, quando a folha e gerada
    # (SIFAPJ01.jcl:18-19 reserva 22:00 com 4 horas).
    day_of_week  = 0
    start_hour   = 3
    start_minute = 0
  }

  tags = var.tags

  lifecycle {
    # Rotacionar a senha nao pode recriar um servidor com 612 milhoes de linhas.
    ignore_changes = [administrator_password, zone]
  }
}

data "azurerm_client_config" "current" {}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "backend" {
  server_name         = azurerm_postgresql_flexible_server.main.name
  resource_group_name = var.resource_group_name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = var.entra_admin_object_id
  principal_name      = var.entra_admin_principal
  principal_type      = "ServicePrincipal"
}

resource "azurerm_postgresql_flexible_server_database" "sifap" {
  name      = "sifap"
  server_id = azurerm_postgresql_flexible_server.main.id
  collation = "pt_BR.utf8"
  charset   = "UTF8"

  lifecycle {
    prevent_destroy = true
  }
}

# ADR-001: a tabela payment e particionada por reference_period. O particionamento
# nativo do PostgreSQL 16 exige que a chave de particao integre a chave primaria.
resource "azurerm_postgresql_flexible_server_configuration" "max_connections" {
  name      = "max_connections"
  server_id = azurerm_postgresql_flexible_server.main.id
  value     = "200"
}

resource "azurerm_postgresql_flexible_server_configuration" "log_min_duration" {
  # Consulta acima de 1s vira log; o legado varre 4,2 milhoes de beneficiarios por ciclo.
  name      = "log_min_duration_statement"
  server_id = azurerm_postgresql_flexible_server.main.id
  value     = "1000"
}

resource "azurerm_key_vault_secret" "administrator_password" {
  # Sem rotacao automatica, uma data de expiracao derruba a aplicacao no vencimento.
  # A expiracao entra junto com a rotacao, nao antes dela.
  #checkov:skip=CKV_AZURE_41:Expiracao depende de rotacao automatizada, ainda nao implementada. Issue #9.
  name         = "psql-administrator-password"
  value        = random_password.administrator.result
  key_vault_id = var.key_vault_id
  content_type = "password"
  tags         = var.tags
}

resource "azurerm_key_vault_secret" "jdbc_url" {
  #checkov:skip=CKV_AZURE_41:URL de conexao sem credencial; expira junto com a rotacao da senha. Issue #9.
  name         = "psql-jdbc-url"
  value        = "jdbc:postgresql://${azurerm_postgresql_flexible_server.main.fqdn}:5432/${azurerm_postgresql_flexible_server_database.sifap.name}?sslmode=require"
  key_vault_id = var.key_vault_id
  content_type = "connection-string"
  tags         = var.tags
}
