package com.app.myfoottrip.ui.view.dialogs

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.databinding.DialogPlaceBottomBinding
import com.app.myfoottrip.ui.view.board.TouchFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.MarkerIcons
import com.rengwuxian.materialedittext.MaterialEditText
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class PlaceBottomDialog(private val listener: OnClickListener,private val place : Place) :
    BottomSheetDialogFragment(), OnMapReadyCallback,
    View.OnClickListener {

    val mTag = "장소 바텀 시트 다이얼로그"

    private lateinit var binding: DialogPlaceBottomBinding
    private var mapFragment: MapFragment = MapFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomInputDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPlaceBottomBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    interface OnClickListener {
        fun onClick(dialog: PlaceBottomDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }

    private fun init(){
        imageSetting()
        initData()
    }

    //이미지 슬라이더
    private fun imageSetting(){
        binding.carouselImage.registerLifecycle(viewLifecycleOwner)

        val list = mutableListOf<CarouselItem>().let {
            it.apply {
                for (image in place.placeImgList!!) {
                    add(CarouselItem(imageUrl = image))
                }
            }
        }
        binding.carouselImage.setData(list)
    }

    //장소 데이터 받아오기
    private fun initData(){
        binding.tvPlaceName.text = place.placeName //장소명
        initMapScroll() //지도
        binding.tvPlaceAddress.text = place.address
    }

    //지도 뷰 스크롤 터치 제어
    private fun initMapScroll(){
        //TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if(mapFragment.isAdded){
            fragmentTransaction.remove( mapFragment )
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_fragment, mapFragment).commit()
        mapFragment.getMapAsync(this)

        binding.mapFragment.setTouchListener(object : TouchFrameLayout.OnTouchListener {
            override fun onTouch() {
                // ScrollView Disallow Touch Event
                binding.placeBottom.requestDisallowInterceptTouchEvent(true)
            }
        })
    }

    override fun onMapReady(naverMap: NaverMap) {
        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(36.02539, 128.380378),  1.0))  // 카메라 위치 (위도,경도,줌)
            .mapType(NaverMap.MapType.Basic)    //지도 유형
            .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)  //빌딩 표시

        MapFragment.newInstance(options)

        val marker = Marker().apply {
            position = LatLng(place.latitude!!, place.longitude!!)
            icon = MarkerIcons.BLACK
            iconTintColor = ContextCompat.getColor(requireContext(),R.color.main)
            width = 60
            height = 80
            map = naverMap
        }

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(place.latitude!!,place.longitude!!)).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)

        naverMap.minZoom = 6.0
        naverMap.maxZoom = 15.0
    }
}