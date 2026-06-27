package com.example.smartnotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartnotes.ui.screens.disciplines.DisciplineListScreen
import com.example.smartnotes.ui.screens.topics.TopicListScreen
import com.example.smartnotes.ui.screens.lectures.LectureListScreen
import com.example.smartnotes.ui.screens.lectures.RecordingScreen
import com.example.smartnotes.ui.screens.lectures.LectureDetailScreen
import com.example.smartnotes.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Disciplines.route) {
        composable(Routes.Disciplines.route) {
            DisciplineListScreen(
                onDisciplineClick = { disciplineId ->
                    navController.navigate(Routes.Topics.createRoute(disciplineId))
                },
                onSettingsClick = {
                    navController.navigate(Routes.Settings.route)
                }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Topics.route,
            arguments = listOf(navArgument("disciplineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val disciplineId = backStackEntry.arguments?.getLong("disciplineId") ?: return@composable
            TopicListScreen(
                disciplineId = disciplineId,
                onTopicClick = { topicId ->
                    navController.navigate(Routes.Lectures.createRoute(topicId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Lectures.route,
            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: return@composable
            LectureListScreen(
                topicId = topicId,
                onLectureClick = { lectureId ->
                    navController.navigate(Routes.LectureDetail.createRoute(lectureId))
                },
                onStartRecording = { lectureId ->
                    navController.navigate(Routes.Recording.createRoute(topicId, lectureId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Recording.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType },
                navArgument("lectureId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: return@composable
            val lectureId = backStackEntry.arguments?.getLong("lectureId") ?: return@composable
            RecordingScreen(
                topicId = topicId,
                lectureId = lectureId,
                onFinish = {
                    navController.popBackStack(Routes.Lectures.createRoute(topicId), false)
                },
                onCancel = {
                    navController.popBackStack(Routes.Lectures.createRoute(topicId), false)
                }
            )
        }
        composable(
            route = Routes.LectureDetail.route,
            arguments = listOf(navArgument("lectureId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lectureId = backStackEntry.arguments?.getLong("lectureId") ?: return@composable
            LectureDetailScreen(
                lectureId = lectureId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
