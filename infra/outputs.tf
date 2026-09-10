output "resource_group_name" {
  description = "Grupo de recursos criado."
  value       = azurerm_resource_group.main.name
}

output "postgresql_fqdn" {
  description = "FQDN do PostgreSQL Flexible Server."
  value       = module.postgresql.server_fqdn
}

output "backend_identity_client_id" {
  description = "Client ID da identidade gerenciada do backend."
  value       = azurerm_user_assigned_identity.backend.client_id
}

output "key_vault_name" {
  description = "Cofre que guarda os segredos da aplicacao."
  value       = azurerm_key_vault.main.name
}

output "jdbc_url_secret_name" {
  description = "Nome do segredo com a URL JDBC. O valor nunca sai do cofre."
  value       = module.postgresql.jdbc_url_secret_name
}
