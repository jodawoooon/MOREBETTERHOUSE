package com.ssafy.happyhouse.service;

import java.util.List;
import java.util.Map;

import com.ssafy.happyhouse.dto.DealDto;
import com.ssafy.happyhouse.dto.DealParamDto;

public interface DealService {
	public DealDto dealDetail(int dealId);

	public List<DealDto> dealList(Map<String, Integer> map);
	public int dealListTotalCnt();

	public List<DealDto> dealListSearchDong(DealParamDto dto);
	public int dealListSearchDongTotalCnt(String searchDong);
	
	public List<DealDto> dealListSearchApt(DealParamDto dto);
	public int dealListSearchAptTotalCnt(String searchApt);
	
	public List<DealDto> dealListBookmark(int limit, int offset, int userSeq);
	public int dealListBookmarkTotalCnt(int userSeq);
}
