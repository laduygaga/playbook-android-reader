package com.playbook.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.playbook.reader.data.repository.BookRepository
import com.playbook.reader.ui.library.LibraryScreen
import com.playbook.reader.ui.library.LibraryViewModel
import com.playbook.reader.ui.reader.ReaderScreen
import com.playbook.reader.ui.reader.ReaderViewModel

class MainActivity : ComponentActivity() {

    private lateinit var bookRepository: BookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookRepository = BookRepository(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlaybookAppNavigation(bookRepository = bookRepository)
                }
            }
        }
    }
}

@Composable
fun PlaybookAppNavigation(bookRepository: BookRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "library"
    ) {
        composable("library") {
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.Factory(bookRepository)
            )
            LibraryScreen(
                viewModel = libraryViewModel,
                onBookClick = { bookId ->
                    navController.navigate("reader/$bookId")
                }
            )
        }

        composable(
            route = "reader/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val readerViewModel: ReaderViewModel = viewModel(
                factory = ReaderViewModel.Factory(bookRepository, bookId)
            )
            ReaderScreen(
                viewModel = readerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
