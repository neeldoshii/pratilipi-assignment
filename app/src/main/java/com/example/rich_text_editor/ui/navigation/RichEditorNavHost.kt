package com.example.rich_text_editor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rich_text_editor.ui.editor.EditorScreen
import com.example.rich_text_editor.ui.editor.EditorViewModel
import com.example.rich_text_editor.ui.posts.PostListScreen
import com.example.rich_text_editor.ui.posts.PostsViewModel

object NavRoutes {
    const val PostList = "post_list"
    const val EditorPattern = "editor/{postId}"
    const val PostIdArg = "postId"

    fun editor(postId: String): String = "editor/$postId"
}

@Composable
fun RichEditorNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.PostList,
        modifier = modifier,
    ) {
        composable(NavRoutes.PostList) {
            val vm: PostsViewModel = hiltViewModel()
            PostListScreen(
                viewModel = vm,
                onOpenPost = { id -> navController.navigate(NavRoutes.editor(id)) },
            )
        }
        composable(
            route = NavRoutes.EditorPattern,
            arguments = listOf(
                navArgument(NavRoutes.PostIdArg) { type = NavType.StringType },
            ),
        ) {
            val vm: EditorViewModel = hiltViewModel()
            EditorScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
