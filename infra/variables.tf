variable "project" {
  description = "Identificador do projeto, usado em nomes e tags."
  type        = string
  default     = "sifap"

  validation {
    condition     = can(regex("^[a-z][a-z0-9]{2,10}$", var.project))
    error_message = "O nome do projeto deve ter de 3 a 11 caracteres minusculos alfanumericos."
  }
}

variable "environment" {
  description = "Ambiente de destino."
  type        = string

  validation {
    condition     = contains(["dev", "hml", "prd"], var.environment)
    error_message = "O ambiente deve ser dev, hml ou prd."
  }
}

variable "location" {
  description = "Regiao do Azure."
  type        = string
  default     = "brazilsouth"
}

variable "owner" {
  description = "Equipe responsavel, registrada na tag owner."
  type        = string
}

variable "postgresql_sku_name" {
  description = "SKU do PostgreSQL Flexible Server."
  type        = string
  default     = "GP_Standard_D2ds_v4"
}

variable "postgresql_storage_mb" {
  # PAYMENT.ddm:172 registra 257 GB e AUDIT.ddm:143 registra 311 GB no legado.
  # O padrao de 512 GB cobre o recorte 001 sem migrar o historico completo.
  description = "Armazenamento do banco em MB."
  type        = number
  default     = 524288

  validation {
    condition     = var.postgresql_storage_mb >= 32768
    error_message = "O armazenamento minimo do Flexible Server e 32768 MB."
  }
}

variable "postgresql_backup_retention_days" {
  description = "Retencao de backup em dias."
  type        = number
  default     = 35
}

variable "allowed_subnet_id" {
  description = "Subnet delegada para acesso privado ao banco. Nulo mantem acesso publico sem regras de firewall."
  type        = string
  default     = null
}
