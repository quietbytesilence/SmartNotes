package com.example.smartnotes.ui.navigation

sealed class Routes(val route: String) {
    data object Disciplines : Routes("disciplines")
    data object Settings : Routes("settings")

    data object Topics : Routes("topics/{disciplineId}") {
        fun createRoute(disciplineId: Long) = "topics/$disciplineId"
    }
    data object Lectures : Routes("lectures/{topicId}") {
        fun createRoute(topicId: Long) = "lectures/$topicId"
    }
    data object Recording : Routes("recording/{topicId}/{lectureId}") {
        fun createRoute(topicId: Long, lectureId: Long) = "recording/$topicId/$lectureId"
    }
    data object LectureDetail : Routes("lecture-detail/{lectureId}") {
        fun createRoute(lectureId: Long) = "lecture-detail/$lectureId"
    }
}
