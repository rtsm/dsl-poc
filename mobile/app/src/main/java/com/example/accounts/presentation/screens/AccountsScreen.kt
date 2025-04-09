package com.example.accounts.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.accounts.domain.repository.AccountRepository
import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.presentation.state.AccountsState
import com.example.accounts.presentation.components.AccountCard
import com.toggl.komposable.architecture.Store
import org.koin.compose.koinInject

@Composable
fun AccountsScreen(
    navController: NavHostController,
    store: Store<AccountsState, AccountsAction>,
) {
    val state by store.state.collectAsState(AccountsState(isLoading = false))
    val repository = koinInject<AccountRepository>()
    val viewModel = viewModel { AccountsViewModel(
        repository = repository,
        store = store
    ) }

    LaunchedEffect(Unit) {
        viewModel.initialise()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.accounts) { account ->
                    AccountCard(
                        account = account,
                        onAccountClick = {
                            store.send(AccountsAction.SelectAccount(account, navController))
                        }
                    )
                }
            }
        }

        state.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = error)
            }
        }
    }
} 