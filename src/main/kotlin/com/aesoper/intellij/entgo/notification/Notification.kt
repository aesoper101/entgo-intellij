package com.aesoper.intellij.entgo.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project


object Notification  {
    private const val notificationGroupId = "Ent notification group"
    private const val notificationLogGroupId = "Ent log group"

    fun log(project: Project, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationLogGroupId)
            .createNotification(content, NotificationType.INFORMATION)
            .notify(project);
    }

    fun info(project: Project, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(content, NotificationType.INFORMATION)
            .notify(project);
    }



    fun warning(project: Project, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(content, NotificationType.WARNING)
            .notify(project);
    }

    fun error(project: Project, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(content, NotificationType.ERROR)
            .notify(project);
    }
}
