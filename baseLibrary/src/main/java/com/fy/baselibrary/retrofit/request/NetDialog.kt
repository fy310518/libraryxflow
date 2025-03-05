//package com.fy.baselibrary.retrofit.request
//
//import android.view.KeyEvent
//import androidx.lifecycle.AndroidViewModel
//import coil3.request.crossfade
//import coil3.request.placeholder
//import com.cozyla.choresreward.R
//import com.cozyla.choresreward.databinding.NetLoadingLayoutDialogBinding
//import com.cozyla.choresreward.util.loadGif
//import com.fy.baselibrary.base.dialog.CommonDialog
//import com.fy.baselibrary.utils.notify.L
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.cancel
//
//class NetDialog(var tScope: CoroutineScope? = null): CommonDialog<AndroidViewModel, NetLoadingLayoutDialogBinding>() {
//
//    override fun initLayoutId(): Int = R.layout.net_loading_layout_dialog
//
//    override fun convertView(dialog: CommonDialog<*, *>?) {
//
//        anim = android.R.style.Animation_Dialog
//
////        vdb.ivLoading.loadGif(R.drawable.drawable_loading) {
////            crossfade(true)
////            placeholder(R.drawable.drawable_loading)
////        }
//
//    }
//
//    override fun setOnKeyListener() {
//        getDialog()?.setOnKeyListener { dialog, keyCode, event ->
//            return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_BACK) {
//                L.e("request", "取消请求")
//                dismiss()
//
//                tScope?.cancel()
//                true
//            } else {
//                false
//            }
//        }
//    }
//}