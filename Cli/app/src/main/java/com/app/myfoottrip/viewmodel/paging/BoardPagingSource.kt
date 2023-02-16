package com.app.myfoottrip.viewmodel.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.app.myfoottrip.model.Board
import com.app.myfoottrip.model.Filter
import com.app.myfoottrip.api.BoardApi
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class BoardPagingSource(
    private val boardService: BoardApi,
    private val filter: Filter,
) : PagingSource<Int, Board>() {
    override fun getRefreshKey(state: PagingState<Int, Board>): Int? {
        return state.anchorPosition?.let { achorPosition ->
            state.closestPageToPosition(achorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(achorPosition)?.nextKey?.minus(1)
        }
    }

    // 데이터 로드
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Board> {
        // LoadParams : 로드할 키와 항목 수 , LoadResult : 로드 작업의 결과
        return try {
            delay(500)
            // 키 값이 없을 경우 기본값을 사용함
            val currentPage = params.key ?: 1
            // 데이터를 제공하는 인스턴스의 메소드 사용
            val response = boardService.getBoardList(currentPage, periodList = filter.periodList, ageList = filter.ageList, themeList = filter.themeList, regionList = filter.regionList, sortedType = filter.sortedType)
            val data = response.body()?: emptyList()
            val responseData = mutableListOf<Board>()
            responseData.addAll(data)
            /* 로드에 성공 시 LoadResult.Page 반환
            data : 전송되는 데이터
            prevKey : 이전 값 (위 스크롤 방향)
            nextKey : 다음 값 (아래 스크롤 방향)
             */
            // 페이지 넘버값 증가
            val nextKey =
                // 마지막 페이지, 데이터 여부 확인
                if (responseData.isEmpty()) {
                    null
                } else {
                    currentPage + 1
                }

            LoadResult.Page(
                data = responseData!!,
                prevKey = null,
                nextKey = nextKey
            )

            // 로드에 실패 시 LoadResult.Error 반환
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}