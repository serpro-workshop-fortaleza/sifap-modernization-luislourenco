# Infraestrutura do SIFAP 2.0 - recorte 001 (geracao mensal da folha).
#
# Escopo deliberadamente minimo: o Estagio 4 operacionaliza o que existe. App Service,
# Front Door e rede dedicada entram quando houver aplicacao implantavel.

locals {
  name_prefix = "${var.project}-${var.environment}"

  # Tags obrigatorias em todo recurso.
  tags = {
    project     = var.project
    environment = var.environment
    owner       = var.owner
    managed_by  = "terraform"
    cost_center = "sifap-modernizacao"
  }
}

resource "azurerm_resource_group" "main" {
  name     = "rg-${local.name_prefix}"
  location = var.location
  tags     = local.tags
}

# Identidade usada pelo backend para autenticar no Key Vault e no PostgreSQL.
# Nenhuma senha de aplicacao circula em string de conexao.
resource "azurerm_user_assigned_identity" "backend" {
  name                = "id-${local.name_prefix}-backend"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  tags                = local.tags
}

module "postgresql" {
  source = "./modules/postgresql"

  name_prefix           = local.name_prefix
  resource_group_name   = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  sku_name              = var.postgresql_sku_name
  storage_mb            = var.postgresql_storage_mb
  backup_retention_days = var.postgresql_backup_retention_days
  geo_redundant_backup  = var.environment == "prd"
  administrator_login   = "sifapadmin"
  allowed_subnet_id     = var.allowed_subnet_id
  key_vault_id          = azurerm_key_vault.main.id
  entra_admin_object_id = azurerm_user_assigned_identity.backend.principal_id
  entra_admin_principal = azurerm_user_assigned_identity.backend.name
  tags                  = local.tags

  depends_on = [azurerm_key_vault_access_policy.terraform]
}

data "azurerm_client_config" "current" {}

resource "azurerm_key_vault" "main" {
  name                       = "kv-${local.name_prefix}-${random_string.suffix.result}"
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 90
  purge_protection_enabled   = var.environment == "prd"
  # Sem acesso publico: somente a rede autorizada alcanca o cofre.
  public_network_access_enabled = false

  network_acls {
    bypass         = "AzureServices"
    default_action = "Deny"
  }

  tags = local.tags
}

resource "random_string" "suffix" {
  length  = 6
  special = false
  upper   = false
}

# Permite ao pipeline gravar o segredo gerado; a aplicacao so le.
resource "azurerm_key_vault_access_policy" "terraform" {
  key_vault_id = azurerm_key_vault.main.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = data.azurerm_client_config.current.object_id

  secret_permissions = ["Get", "List", "Set", "Delete", "Purge", "Recover"]
}

resource "azurerm_key_vault_access_policy" "backend" {
  key_vault_id = azurerm_key_vault.main.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_user_assigned_identity.backend.principal_id

  secret_permissions = ["Get", "List"]
}

resource "azurerm_log_analytics_workspace" "main" {
  name                = "log-${local.name_prefix}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.tags
}
