package com.example.accounts.presentation.reducer

import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.presentation.state.AccountsState
import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.Mutable
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.mutateWithoutEffects

class AccountsReducer : Reducer<AccountsState, AccountsAction> {
    override fun reduce(
        state: Mutable<AccountsState>,
        action: AccountsAction
    ): List<Effect<AccountsAction>> {
        return when (action) {
            is AccountsAction.LoadAccounts -> {
                state.mutateWithoutEffects {
                    state.invoke().copy(
                        accounts = action.accounts,
                        isLoading = false
                    )
                }
            }

            is AccountsAction.SelectAccount -> {
                state.mutateWithoutEffects {
                    state.invoke().copy(
                        selectedAccount = action.account
                    ).also {
                        action.navController.navigate("profile")
                    }
                }
            }

            is AccountsAction.ShowError -> {
                state.mutateWithoutEffects {
                    state.invoke().copy(
                        error = action.message,
                        isLoading = false
                    )
                }
            }

            is AccountsAction.Loading -> {
                state.mutateWithoutEffects {
                    state.invoke().copy(
                        isLoading = true
                    )
                }
            }
        }
    }
} 