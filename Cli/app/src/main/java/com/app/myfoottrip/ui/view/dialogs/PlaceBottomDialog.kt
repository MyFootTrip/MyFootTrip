package com.app.myfoottrip.ui.view.dialogs

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rengwuxian.materialedittext.MaterialEditText
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class PlaceBottomDialog(private val listener: OnClickListener,private val place : Place) :
    BottomSheetDialogFragment(),
    View.OnClickListener {

    lateinit var sendBtn: ImageView
    lateinit var comment : MaterialEditText
    val mTag = "댓글 입력 다이얼로그"

    lateinit var imageSlider: ImageCarousel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomInputDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view: View = inflater.inflate(R.layout.dialog_place_bottom, container, false)

        //뷰 연결
        imageSlider = view.findViewById(R.id.carousel_image)
//        sendBtn = view.findViewById(R.id.iv_send)!!
//        comment = view.findViewById(R.id.et_comment)
//        sendBtn.setOnClickListener(this)

        imageSetting()

        return view
    }

    interface OnClickListener {
        fun onClick(dialog: PlaceBottomDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }

    //이미지 슬라이더
    private fun imageSetting(){
        imageSlider.registerLifecycle(viewLifecycleOwner)

        val list = mutableListOf<CarouselItem>().let {
            it.apply {
                for (image in place.placeImgList!!) {
                    add(CarouselItem(imageUrl = image))
                }
            }
        }

        imageSlider.setData(list)
    }
}