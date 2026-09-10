output "server_id" {
  description = "ID do Flexible Server."
  value       = azurerm_postgresql_flexible_server.main.id
}

output "server_fqdn" {
  description = "FQDN do servidor."
  value       = azurerm_postgresql_flexible_server.main.fqdn
}

output "database_name" {
  description = "Nome do banco da aplicacao."
  value       = azurerm_postgresql_flexible_server_database.sifap.name
}

output "jdbc_url_secret_name" {
  # O valor fica no cofre; a saida devolve somente o nome do segredo.
  description = "Nome do segredo do Key Vault que guarda a URL JDBC."
  value       = azurerm_key_vault_secret.jdbc_url.name
}
