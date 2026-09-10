variable "name_prefix" {
  description = "Prefixo de nomenclatura no formato projeto-ambiente."
  type        = string
}

variable "resource_group_name" {
  description = "Grupo de recursos de destino."
  type        = string
}

variable "location" {
  description = "Regiao do Azure."
  type        = string
}

variable "sku_name" {
  description = "SKU do Flexible Server."
  type        = string
}

variable "storage_mb" {
  description = "Armazenamento em MB."
  type        = number
}

variable "backup_retention_days" {
  description = "Retencao de backup em dias."
  type        = number
}

variable "geo_redundant_backup" {
  description = "Habilita backup com redundancia geografica."
  type        = bool
  default     = false
}

variable "administrator_login" {
  description = "Login administrativo local."
  type        = string
}

variable "allowed_subnet_id" {
  description = "Subnet delegada para acesso privado. Nulo mantem acesso publico sem regras de firewall."
  type        = string
  default     = null
}

variable "key_vault_id" {
  description = "Cofre onde a senha administrativa e persistida."
  type        = string
}

variable "entra_admin_object_id" {
  description = "Object ID da identidade gerenciada com papel de administrador Entra ID."
  type        = string
}

variable "entra_admin_principal" {
  description = "Nome do principal administrador Entra ID."
  type        = string
}

variable "tags" {
  description = "Tags obrigatorias aplicadas a todo recurso."
  type        = map(string)
}
