package de.geosphere.speechplaning.core.model

import de.geosphere.speechplaning.core.model.data.UserRole

fun AppUser.canEditSpeeches(): Boolean {
    return this.role == UserRole.ADMIN || this.role == UserRole.SPEAKING_PLANER
}

fun AppUser.canManageUsers(): Boolean {
    return this.role == UserRole.ADMIN || this.role == UserRole.SPEAKING_PLANER
}

fun AppUser.canViewInternalNotes(): Boolean {
    return this.role != UserRole.SPEAKING_ASSISTANT
}
