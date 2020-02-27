/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 27.02.20 17:04
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mmdev.business.core.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.ui.core.BaseFragment
import com.mmdev.roove.ui.core.viewmodel.RemoteUserRepoViewModel
import com.mmdev.roove.ui.core.viewmodel.SharedViewModel
import com.mmdev.roove.utils.observeOnce
import com.mmdev.roove.utils.showToastText
import kotlinx.android.synthetic.main.fragment_settings_edit_info.*


/**
 * This is the documentation block about the class
 */

class SettingsEditInfoFragment: BaseFragment(R.layout.fragment_settings_edit_info) {

	private lateinit var userItem: UserItem

	private var name = ""
	private var age = 0
	private var city = ""
	private var gender = ""
	private var preferredGender = ""

	private var cityToDisplay = ""

	private var descriptionText = ""

	private lateinit var cityList: Map<String, String>
	private lateinit var genderList: List<String>
	private lateinit var preferredGenderList: List<String>

	private lateinit var remoteRepoViewModel: RemoteUserRepoViewModel
	private lateinit var sharedViewModel: SharedViewModel

	companion object{
		private const val TAG = "mylogs_SettingsAccFragment"
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		cityList = mapOf(context.getString(R.string.russia_ekb) to "ekb",
		                 context.getString(R.string.russia_krasnoyarsk) to "krasnoyarsk",
		                 context.getString(R.string.russia_krd) to "krd",
		                 context.getString(R.string.russia_kzn) to "kzn",
		                 context.getString(R.string.russia_msk) to "msk",
		                 context.getString(R.string.russia_nnv) to "nnv",
		                 context.getString(R.string.russia_nsk) to "nsk",
		                 context.getString(R.string.russia_sochi) to "sochi",
		                 context.getString(R.string.russia_spb) to "spb")

		genderList = listOf("male", "female")
		preferredGenderList = listOf("male", "female", "everyone")

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		activity?.run {
			remoteRepoViewModel= ViewModelProvider(this, factory)[RemoteUserRepoViewModel::class.java]
			sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]

		} ?: throw Exception("Invalid Activity")

		sharedViewModel.getCurrentUser().observeOnce(this, Observer {
			userItem = it
		})

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		initProfile()

		changerNameSetup()
		changerGenderSetup()
		changerPreferredGenderSetup()
		changerAgeSetup()
		changerCitySetup()

		btnSettingsEditSave.setOnClickListener {
			remoteRepoViewModel.updateUserItem(userItem)
			remoteRepoViewModel.getUserUpdateStatus().observeOnce(this, Observer {
				if (it) {
					context?.showToastText("Successfully saved")
					sharedViewModel.userSelected.value = userItem
				}
			})
		}
	}


	private fun initProfile() {
		if (this::userItem.isInitialized) {
			edSettingsEditName.setText(userItem.baseUserInfo.name)
			dropSettingsEditGender.setText(userItem.baseUserInfo.gender)
			dropSettingsEditPreferredGender.setText(userItem.baseUserInfo.preferredGender)
			tvSettingsEditAge.text = "Age: ${userItem.baseUserInfo.age}"
			sliderSettingsEditAge.value = userItem.baseUserInfo.age.toFloat()

			cityToDisplay = cityList.filterValues { it == userItem.baseUserInfo.city }.keys.first()
			dropSettingsEditCity.setText(cityToDisplay)

			edSettingsEditDescription.setText(userItem.aboutText)
		}
	}

	private fun changerNameSetup() {
		edSettingsEditName.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				layoutSettingsEditName.isCounterEnabled = true
			}

			override fun afterTextChanged(s: Editable) {
				when {
					s.length > layoutSettingsEditName.counterMaxLength -> {
						layoutSettingsEditName.error = "Max length reached"
						btnSettingsEditSave.isEnabled = false
					}
					s.isEmpty() -> {
						layoutSettingsEditName.error = "Name must not be empty"
						btnSettingsEditSave.isEnabled = false

					}
					else -> {
						layoutSettingsEditName.error = ""
						name = s.toString().trim()
						userItem.baseUserInfo.name = name
						btnSettingsEditSave.isEnabled = true
					}
				}
			}
		})

		edSettingsEditName.setOnEditorActionListener { v, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				v.text = v.text.toString().trim()
				v.clearFocus()
			}
			return@setOnEditorActionListener false
		}

		edSettingsEditDescription.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				layoutSettingsEditName.isCounterEnabled = true
			}

			override fun afterTextChanged(s: Editable) {
				if (s.length > layoutSettingsEditDescription.counterMaxLength){
					layoutSettingsEditDescription.error = "Max length reached"
					btnSettingsEditSave.isEnabled = false
				}
				else {
					layoutSettingsEditDescription.error = ""
					descriptionText = s.toString().trim()
					userItem.aboutText = descriptionText
					btnSettingsEditSave.isEnabled = true
				}
			}
		})

		edSettingsEditDescription.setOnEditorActionListener { v, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				v.text = v.text.toString().trim()
				v.clearFocus()
			}
			return@setOnEditorActionListener false
		}

	}

	private fun changerGenderSetup() {
		val genderAdapter = ArrayAdapter<String>(context!!,
		                                         R.layout.drop_text_item,
		                                         genderList)
		dropSettingsEditGender.setAdapter(genderAdapter)

		dropSettingsEditGender.setOnItemClickListener { _, _, position, _ ->
			gender = genderList[position]
			userItem.baseUserInfo.gender = gender
		}
	}

	private fun changerPreferredGenderSetup() {
		val preferredGenderAdapter = ArrayAdapter<String>(context!!,
		                                                  R.layout.drop_text_item,
		                                                  preferredGenderList)

		dropSettingsEditPreferredGender.setAdapter(preferredGenderAdapter )

		dropSettingsEditPreferredGender.setOnItemClickListener { _, _, position, _ ->
			preferredGender = preferredGenderList[position]
			userItem.baseUserInfo.preferredGender = preferredGender
		}
	}

	private fun changerCitySetup() {
		val cityAdapter = ArrayAdapter<String>(context!!,
		                                       R.layout.drop_text_item,
		                                       cityList.map { it.key })
		dropSettingsEditCity.setAdapter(cityAdapter)

		dropSettingsEditCity.setOnItemClickListener { _, _, position, _ ->
			city = cityList.map { it.value }[position]
			cityToDisplay = cityList.map { it.key }[position]
			userItem.baseUserInfo.city = city
		}
	}

	private fun changerAgeSetup() {
		sliderSettingsEditAge.setOnChangeListener{ _, value ->
			age = value.toInt()
			userItem.baseUserInfo.age = age
			tvSettingsEditAge.text = "Age: $age"
		}
	}

	override fun onBackPressed() {
		super.onBackPressed()
		findNavController().navigateUp()
	}
}