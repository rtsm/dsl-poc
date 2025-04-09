package com.example.accounts.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accounts.domain.repository.AccountRepository
import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.presentation.state.AccountsState
import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class AccountsViewModel(
    private val store: Store<AccountsState, AccountsAction>,
    private val repository: AccountRepository,
) : ViewModel(), KoinComponent {

    fun initialise() {
        store.send(AccountsAction.Loading)
        viewModelScope.launch {
            try {
                repository.getAccounts().collectLatest {
                    store.send(AccountsAction.LoadAccounts(repository.getAccounts().first()))
                }
            } catch (ex: Exception) {
                notifyError(ex)
            }
        }
    }

    private fun notifyError(ex: Exception) {
        store.send(AccountsAction.ShowError("error loading accounts!"))
    }
}