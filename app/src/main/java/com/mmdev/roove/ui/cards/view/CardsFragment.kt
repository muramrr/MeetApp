package com.mmdev.roove.ui.cards.view


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mmdev.business.cards.model.CardItem
import com.mmdev.roove.R
import com.mmdev.roove.core.GlideApp
import com.mmdev.roove.core.injector
import com.mmdev.roove.ui.cards.viewmodel.CardsViewModel
import com.mmdev.roove.ui.custom.LoadingDialog
import com.mmdev.roove.ui.main.view.MainActivity
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class CardsFragment: Fragment(R.layout.fragment_card) {

	private lateinit var mMainActivity: MainActivity

	private lateinit var cardStackView: CardStackView
	private val mCardsStackAdapter: CardsStackAdapter = CardsStackAdapter(listOf())
	private lateinit var progressDialog: LoadingDialog
	private var mProgressShowing: Boolean = false

	private lateinit var mSwipedCardItem: CardItem

	private lateinit var cardsViewModel: CardsViewModel
	private val cardsViewModelFactory = injector.cardsViewModelFactory()

	private val disposables = CompositeDisposable()

	companion object{
		fun newInstance(): CardsFragment {
			return CardsFragment()
		}
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activity?.let { mMainActivity = it as MainActivity }
		cardsViewModel = ViewModelProvider(mMainActivity, cardsViewModelFactory).get(CardsViewModel::class.java)
		progressDialog = LoadingDialog(mMainActivity)


	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		cardStackView = view.findViewById(R.id.card_stack_view)

		//get potential users
		disposables.add(cardsViewModel.getPotentialUserCards()
			                .observeOn(AndroidSchedulers.mainThread())
			                .doOnSubscribe { showLoadingDialog() }
			                .doFinally { hideLoadingDialog() }
			                .subscribe({
				                           Log.wtf("mylogs", "cards to show: ${it.size}")
				                           mCardsStackAdapter.updateData(it)
				                           if(it.isNotEmpty()) hideLoadingDialog()
			                           },
			                           {
				                           Log.wtf("mylogs", "get potential users error + $it")
			                           }))

		val cardStackLayoutManager = CardStackLayoutManager(mMainActivity, object: CardStackListener {

			override fun onCardAppeared(view: View, position: Int) {
				//get current displayed on card profile
				mSwipedCardItem = mCardsStackAdapter.getSwipeProfile(position)

			}

			override fun onCardDragging(direction: Direction, ratio: Float) {}

			override fun onCardSwiped(direction: Direction) {
				//if right = add to liked
				//else = add to skiped
				if (direction == Direction.Right) {
					disposables.add(cardsViewModel.handlePossibleMatch(mSwipedCardItem)
						                .subscribe({
							                           if (it) showMatchDialog(mSwipedCardItem)
							                           Log.wtf("mylogs", mSwipedCardItem.toString())
						                           },
						                           {
							                           Log.wtf("mylogs", it)
						                           }))
				}
				else cardsViewModel.addToSkipped(mSwipedCardItem)
			}

			override fun onCardRewound() {}

			override fun onCardCanceled() {}

			override fun onCardDisappeared(view: View, position: Int) {
				//if there is no available user to show - show loading
				if (position == mCardsStackAdapter.itemCount - 1) {
					mCardsStackAdapter.notifyDataSetChanged()
					showLoadingDialog()
				}
			}

		})

		cardStackView.apply {
			adapter = mCardsStackAdapter
			layoutManager = cardStackLayoutManager
		}

	}


	private fun showLoadingDialog() {
		if (!mProgressShowing) {
			cardStackView.visibility = View.GONE
			progressDialog.showDialog()
			mProgressShowing = true
		}
	}

	private fun hideLoadingDialog() {
		if (mProgressShowing) {
			progressDialog.dismissDialog()
			cardStackView.visibility = View.VISIBLE
			mProgressShowing = false
		}
	}

	private fun showMatchDialog(matchCardItem: CardItem) {
		val matchDialog = Dialog(mMainActivity)
		matchDialog.setContentView(R.layout.dialog_match)
		//matchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		//matchDialog.getWindow().setDimAmount(0.87f);
		matchDialog.show()
		matchDialog.window!!.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
		val backgr = matchDialog.findViewById<ImageView>(R.id.diag_match_iv_backgr_profile_img)
		GlideApp.with(this@CardsFragment)
			.load(matchCardItem.mainPhotoUrl)
			.centerInside()
			.into(backgr)
		matchDialog.findViewById<View>(R.id.diag_match_tv_keep_swp).setOnClickListener { matchDialog.dismiss() }
	}


	override fun onResume() {
		super.onResume()
		mMainActivity.toolbar.title = "Cards"
	}

	override fun onDestroy(){
		super.onDestroy()
		disposables.clear()

	}
}
