/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 16.02.20 18:03
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.profile.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmdev.business.conversations.ConversationItem
import com.mmdev.business.user.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.ui.core.BaseFragment
import com.mmdev.roove.ui.core.ImagePagerAdapter
import com.mmdev.roove.ui.core.SharedViewModel
import com.mmdev.roove.ui.core.viewmodel.RemoteUserRepoViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

/**
 * This is the documentation block about the class
 */

class ProfileFragment: BaseFragment(R.layout.fragment_profile) {


	private val userPhotosAdapter = ImagePagerAdapter(listOf())
	private val mUserPlacesToGoAdapter = ProfilePlacesToGoAdapter(listOf())

	private var fabVisible: Boolean = false

	//saving state
	private lateinit var selectedUser: UserItem
	private var isOnCreateCalled: Boolean = false

	private lateinit var sharedViewModel: SharedViewModel
	private lateinit var remoteRepoViewModel: RemoteUserRepoViewModel


	companion object{
		private const val FAB_VISIBLE_KEY = "FAB_VISIBLE"
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			fabVisible = it.getBoolean(FAB_VISIBLE_KEY)
		}

		sharedViewModel = activity?.run {
			ViewModelProvider(this, factory)[SharedViewModel::class.java]
		} ?: throw Exception("Invalid Activity")

		remoteRepoViewModel = ViewModelProvider(this, factory)[RemoteUserRepoViewModel::class.java]

		sharedViewModel.userCardSelected.observe(this, Observer { carditem ->
			//block to sharedviewmodel update card clicked on another screen
			if (!isOnCreateCalled) {
				remoteRepoViewModel.getFullUserItem(carditem.baseUserInfo)
				remoteRepoViewModel.getUser().observe(this, Observer {
					selectedUser = it
					collapseBarProfile.title = selectedUser.baseUserInfo.name
					userPhotosAdapter.updateData(selectedUser.photoURLs.toList())
					mUserPlacesToGoAdapter.updateData(selectedUser.placesToGo.toList())
					isOnCreateCalled = true
				})
			}

		})
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		viewPagerProfilePhotos.apply {
			(getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
			adapter = userPhotosAdapter
		}

		TabLayoutMediator(tlDotsIndicatorProfile, viewPagerProfilePhotos){
			_: TabLayout.Tab, _: Int ->
			//do nothing
		}.attach()

		toolbarProfile.apply {
			//menu declared directly in xml
			//no need to inflate menu manually
			//set only title and actions
			setNavigationOnClickListener { findNavController().navigateUp() }
			setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.action_report -> { Toast.makeText(context,
					                                       "action report click",
					                                       Toast.LENGTH_SHORT).show()
					}
				}
				return@setOnMenuItemClickListener true
			}
		}

		rvProfileWantToGoList.apply { adapter = mUserPlacesToGoAdapter }

		mUserPlacesToGoAdapter.setOnItemClickListener(object: ProfilePlacesToGoAdapter.OnItemClickListener {

			override fun onItemClick(view: View, position: Int) {

				val placeId = bundleOf("PLACE_ID" to
						                       mUserPlacesToGoAdapter.getPlaceToGoItem(position).id)

				findNavController().navigate(R.id.action_profile_to_placeDetailedFragment, placeId)

			}

		})

		if (fabVisible) {
			fabProfileSendMessage.setOnClickListener {

				findNavController().navigate(R.id.action_profile_to_chatFragment)

				sharedViewModel.setConversationSelected(ConversationItem(selectedUser.baseUserInfo))
			}
		}
		else fabProfileSendMessage.visibility = View.GONE

	}

	override fun onResume() {
		super.onResume()
		if (isOnCreateCalled) {
			collapseBarProfile.title = selectedUser.baseUserInfo.name
			userPhotosAdapter.updateData(selectedUser.photoURLs.toList())
		}
	}

	override fun onBackPressed() {
		findNavController().navigateUp()
	}


}