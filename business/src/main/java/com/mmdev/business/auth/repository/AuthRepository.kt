/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 16.02.20 16:05
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.auth.repository

import com.mmdev.business.user.UserItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single


interface AuthRepository {

	fun isAuthenticatedListener(): Observable<Boolean>

	fun signIn(token: String): Single<HashMap<Boolean, UserItem>>

	fun registerUser(userItem: UserItem): Completable

	fun logOut()

}