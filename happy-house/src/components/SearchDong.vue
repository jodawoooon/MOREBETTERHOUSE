<template>
  <main class="content">
    <div class="row">
      <div class="col-12 mb-4">
        <div class="card border-light shadow-sm ">
          <div class="card-body">
            <!-- selectbar start  -->
            <div class="d-flex justify-content-center mb-2" style="height:100px;">
              <div class="row">
                <div class="col align-self-center">
                  <select v-model="selectedSidoCode" class="form-select select" aria-label="시">
                    <option value="empty" selected disabled>시/도</option>
                    <option v-for="(sido, index) in selectSidoList" :key="index" :value="sido.SIDO_CODE">{{ sido.SIDO_NAME }}</option>
                  </select>
                </div>
                <div class="col align-self-center">
                  <select v-model="selectedGugunCode" class="form-select select" aria-label="구">
                    <option value="empty" selected disabled>구/군</option>
                    <option v-for="(gugun, index) in selectGugunList" :key="index" :value="gugun.GUGUN_CODE">{{ gugun.GUGUN_NAME }}</option>
                  </select>
                </div>
                <div class="col align-self-center">
                  <select v-model="selectedDongCode" class="form-select select" aria-label="동">
                    <option value="empty" selected disabled>동</option>
                    <option v-for="(dong, index) in selectDongList" :key="index" :value="dong.DONG_CODE">{{ dong.DONG_NAME }}</option>
                  </select>
                </div>
                <div class="col align-self-center">
                  <input type="button" class="btn btn-secondary ml-3" value="검색" @click="btnSearchClick" :disabled="selectedDongCode == 'empty' || loadingCount != 0" />
                </div>
                <div class="col align-self-center">
                  <input
                    type="button"
                    :value="isBookmarked ? '관심 지역 해제' : '관심 지역 등록'"
                    class="btn ml-3"
                    :class="isBookmarked ? 'btn-warning' : 'btn-outline-warning'"
                    @click="clickBookmarkArea"
                    :disabled="selectedDongCode == 'empty'"
                  />
                </div>
              </div>
            </div>
            <!-- selectbar end  -->
            <div class="row m-1" style="height:550px;">
              <!-- pulseLoader -->
              <div v-if="loadingCount != 0" class="d-flex align-items-center justify-content-center" style="height:100%;">
                <ring-loader :loading="loadingCount != 0" color="#f3b773"></ring-loader>
              </div>
              <div v-else class="col-4" id="apartInfo">
                <!-- aptInfo start -->
                <div>
                  <div v-for="(house, index) in houseList" :key="index" class="border-light apart" :id="'apartInfo' + (index + 1)">
                    <div class=" cust_box row p-3 pt-4 rounded" style=" margin-right:10px;">
                      <div class="col-9 " @click="clickAptInfo(house)" style="cursor: pointer; padding-left:30px">
                        <h5>{{ house.aptName }}</h5>
                        <p class="m-0">거래금액: {{ house.dealAmount }}</p>
                        <p class="m-0">전용면적: {{ house.area }}</p>
                        <p class="m-0">등록일: {{ makeDateStr(house.dealYear, house.dealMonth, house.dealDay, '.') }}</p>
                      </div>
                      <div class="col-3 align-self-center" style="text-align: center;">
                        <font-awesome-icon
                          :icon="[house.bookmarked ? 'fas' : 'far', 'star']"
                          @click="clickBookmark(house)"
                          :id="'bookmarkStar' + (index + 1)"
                          aria-hidden="true"
                          style="color: rgb(255, 226, 95); font-size: 25px; cursor: pointer;"
                          class=" scale-up-5"
                        >
                          <input type="hidden" value="' + dealNo + '" />
                        </font-awesome-icon>
                      </div>

                      <div class="text-center rounded-bottom" @click="clickAptInfo(house)" style="cursor: pointer;">
                        <h6 class="m-0 text-center text-white">move</h6>
                      </div>
                    </div>
                    <hr style="margin-top:0px; margin-left:30px; margin-right:20px; margin-bottom:0px;" />
                  </div>
                  <hr style="margin-top:0px; margin-left:30px; margin-right:20px; margin-bottom:0px;" />
                </div>
                <!-- aptInfo end -->
                
              </div>
              <div id="map" class="col" style=" width:100%; height: 550px"></div>
            </div>
          </div>
          <div class="mt-4" v-if="loadingCount == 0">
            <pagination :listRowCount="listRowCount" :pageLinkCount="pageLinkCount" :currentPageIndex="currentPageIndex" :houseListcount="houseListCount" @call-parent="movePage"></pagination>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import http from '@/common/axios.js';
import router from '@/routers/routers.js';
import RingLoader from 'vue-spinner/src/RingLoader.vue';
import Pagination from './Pagination.vue';
import { mapGetters } from 'vuex';

export default {
  name: 'SearchDong',
  components: {
    RingLoader,
    Pagination,
  },
  data() {
    return {
      loadingCount: 0,

      houseList: [],
      houseListCount: 0,
      limit: 10,
      offset: 0,

      // pagination
      listRowCount: 10,
      pageLinkCount: 10,
      currentPageIndex: 1,

      selectSidoList: [],
      selectGugunList: [],
      selectDongList: [],
      selectedSidoCode: 'empty',
      selectedGugunCode: 'empty',
      selectedDongCode: 'empty',

      map: '',

      isBookmarked: false,
    };
  },
  methods: {
    loadingCountUp() {
      this.loadingCount++;
    },
    loadingCountDown() {
      this.loadingCount--;
    },
    btnSearchClick() {
      this.currentPageIndex = 1;
      this.offset = 0;
      this.searchList();
    },
    searchList() {
      console.log('searchList() is called!!!!!!');
      if (this.selectedDongCode == 'empty') {
        console.log('this.selectedDongCode is empty');
        this.loadingCountUp();
        http
          .get('/house', {
            params: {
              limit: this.limit,
              offset: this.offset,
              searchType: 'init',
              userSeq: this.getUserSeq,
            },
          })
          .then(({ data }) => {
            console.log('searchList : ');
            console.log(data);
            this.loadingCountDown();
            if (data.result == 'login') {
              console.log(data.result);
              router.push('/login');
            } else {
              this.houseList = data.list;
              this.houseListCount = data.count;
              if (this.houseListCount == 0) {
                this.$alertify.warning('검색 결과가 없습니다.');
              } else this.kakaoMap();
            }
          })
          .catch((error) => {
            console.log(error);
          });
      } else {
        console.log('this.selectedDongCode is not empty');
        this.loadingCountUp();
        http
          .get('/house', {
            params: {
              limit: this.limit,
              offset: this.offset,
              searchWord: this.selectedDongCode,
              searchType: 'dong',
              userSeq: this.getUserSeq,
            },
          })
          .then(({ data }) => {
            console.log('searchList : ');
            console.log(data);
            this.loadingCountDown();
            if (data.result == 'login') {
              router.push('/login');
            } else {
              this.houseList = data.list;
              this.houseListCount = data.count;
              if (this.houseListCount == 0) {
                this.$alertify.warning('검색 결과가 없습니다.');
              } else this.kakaoMap();
            }
          });
      }
    },

    clickBookmark(house) {
      console.log('userSeq : ' + this.getUserSeq);
      console.log('dealNo : ' + house.no);
      if (house.bookmarked) {
        console.log('house.bookmarked : ' + house.bookmarked);
        http
          .delete('/bookmark', {
            params: {
              userSeq: this.getUserSeq,
              dealNo: house.no,
            },
          })
          .then(({ data }) => {
            console.log('deleteBookmark!!!!');
            console.log(data);
            if (data.result == 'login') {
              router.push('/login');
            } else this.$alertify.error('관심 매물을 삭제하였습니다.');
          })
          .catch((error) => {
            console.log(error);
          });
      } else {
        console.log('house.bookmarked : ' + house.bookmarked);
        http
          .post('/bookmark', {
            userSeq: this.getUserSeq,
            dealNo: house.no,
          })
          .then(({ data }) => {
            console.log('insertBookmark!!!!!!');
            console.log(data);
            if (data.result == 'login') {
              router.push('/login');
            } else this.$alertify.success('관심 매물을 추가하였습니다.');
          })
          .catch((error) => {
            console.log(error);
          });
      }
      house.bookmarked = !house.bookmarked;
    },

    clickBookmarkArea() {
      console.log('clickBookmarkArea()!!!!');
      console.log('isBookmarked : ' + this.isBookmarked);
      if (this.isBookmarked) {
        http
          .delete('/bookmarkArea', {
            params: {
              userSeq: this.getUserSeq,
              dongCode: this.selectedDongCode,
            },
          })
          .then(({ data }) => {
            console.log('deleteBookmark!!!!!!');
            console.log(data);
            if (data.result == 'login') {
              router.push('/login');
            } else this.$alertify.error('관심 지역을 해제하였습니다.');
          })
          .catch((error) => {
            console.log(error);
          });
      } else {
        http
          .post('/bookmarkArea', {
            userSeq: this.getUserSeq,
            dongCode: this.selectedDongCode,
          })
          .then(({ data }) => {
            console.log('insertBookmark!!!!!!');
            console.log(data);
            if (data.result == 'login') {
              router.push('/login');
            } else this.$alertify.success('관심 지역을 추가하였습니다.');
          })
          .catch((error) => {
            console.log(error);
          });
      }
      this.isBookmarked = !this.isBookmarked;
    },

    sidoList() {
      console.log('sidoList() is called!!!!!!');
      http.get('/sido').then(({ data }) => {
        console.log('sidoList : ');
        console.log(data);
        if (data.result == 'login') {
          router.push('/login');
        } else {
          this.selectSidoList = data;
          if (this.$route.params.select != undefined) {
            this.selectedSidoCode = this.$route.params.select.sido.SIDO_CODE;
          }
        }
      });
    },
    gugunList() {
      console.log('gugunList() is called!!!!!!');
      http
        .get('/gugun', {
          params: {
            sidoCode: this.selectedSidoCode,
          },
        })
        .then(({ data }) => {
          console.log('gugunList : ');
          console.log(data);
          if (data.result == 'login') {
            router.push('/login');
          } else {
            this.selectGugunList = data;
            if (this.$route.params.select != undefined) {
              this.selectedGugunCode = this.$route.params.select.gugun.GUGUN_CODE;
            }
          }
        });
    },
    dongList() {
      console.log('dongList() is called!!!!!!!');
      http
        .get('/dong', {
          params: {
            sidoCode: this.selectedSidoCode,
            gugunCode: this.selectedGugunCode,
          },
        })
        .then(({ data }) => {
          console.log('dongList : ');
          console.log(data);
          if (data.result == 'login') {
            router.push('/login');
          } else {
            this.selectDongList = data;
            if (this.$route.params.select != undefined) {
              this.selectedDongCode = this.$route.params.select.dong.DONG_CODE;
              this.searchList();
            }
          }
        });
    },
    // pagination
    movePage(pageIndex) {
      console.log('SearchDong.vue : movePage : pageIndex : ' + pageIndex);
      this.offset = (pageIndex - 1) * this.listRowCount;
      this.currentPageIndex = pageIndex;

      this.searchList();
    },

    makeDateStr(year, month, day, type) {
      //2010.05.05
      if (type == '.') {
        return year + '.' + (month < 10 ? '0' + month : month) + '.' + (day < 10 ? '0' + day : day);
      }
    },

    kakaoMap() {
      if (window.kakao && window.kakao.maps) {
        this.initMap();
      } else {
        const script = document.createElement('script');
        /* global kakao */
        script.onload = () => kakao.maps.load(this.initMap);
        script.src = 'http://dapi.kakao.com/v2/maps/sdk.js?appkey=6b6db1abf1494d99829a0aceb281502f';
        document.head.appendChild(script);
      }
    },

    initMap() {
      var mapContainer = document.getElementById('map'), // 지도를 표시할 div
        mapOption = {
          center: new kakao.maps.LatLng(this.houseList[0].lat, this.houseList[0].lng), // 지도의 중심좌표
          level: 4, // 지도의 확대 레벨
        };

      this.map = new kakao.maps.Map(mapContainer, mapOption);
      // 일반 지도와 스카이뷰로 지도 타입을 전환할 수 있는 지도타입 컨트롤을 생성합니다
      var mapTypeControl = new kakao.maps.MapTypeControl();

      // 지도에 컨트롤을 추가해야 지도위에 표시됩니다
      // kakao.maps.ControlPosition은 컨트롤이 표시될 위치를 정의하는데 TOPRIGHT는 오른쪽 위를 의미합니다
      this.map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

      // 지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
      var zoomControl = new kakao.maps.ZoomControl();
      this.map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

      var positions = [];
      for (var i = 0; i < this.houseList.length; i++) {
        positions.push({
          // var windowDiv = '<div class="m-4">' + document.getElementById(school).innerHTML + "</div>";
          // '<div class="col-8">' + document.getElementById('apartInfo' + (i + 1)).innerHTML + '</div>',
          // content: `<div class="col-8">
          //   <h5>${this.houseList[i].aptName}</h5>
          //   <p class="m-0">거래금액: ${this.houseList[i].dealAmount}</p>
          //   <p class="m-0">전용면적: ${this.houseList[i].area}</p>
          //   <p class="m-0">등록일: ${this.makeDateStr(this.houseList[i].dealYear, this.houseList[i].dealMonth, this.houseList[i].dealDay, '.')}</p>
          // </div>`,
          content: `<div class="m-4 mt-4" style="width:200px">
                      <h5>${this.houseList[i].aptName}</h5>
                      <p class="m-0">거래금액: ${this.houseList[i].dealAmount}</p>
                      <p class="m-0">전용면적: ${this.houseList[i].area}</p>
                      <p class="">등록일: ${this.makeDateStr(this.houseList[i].dealYear, this.houseList[i].dealMonth, this.houseList[i].dealDay, '.')}</p>
                    </div>`,
          latlng: new kakao.maps.LatLng(this.houseList[i].lat, this.houseList[i].lng),
        });
      }

      for (var j = 0; j < positions.length; j++) {
        var marker = new kakao.maps.Marker({
          map: this.map,
          position: positions[j].latlng, // 마커를 표시할 위치
        });

        var infowindow = new kakao.maps.InfoWindow({
          content: positions[j].content, // 인포윈도우에 표시할 내용
        });

        kakao.maps.event.addListener(marker, 'mouseover', makeOverListener(this.map, marker, infowindow));
        kakao.maps.event.addListener(marker, 'mouseout', makeOutListener(infowindow));
      }

      // 인포윈도우를 표시하는 클로저를 만드는 함수입니다
      function makeOverListener(map, marker, infowindow) {
        return function() {
          infowindow.open(map, marker);
        };
      }

      // 인포윈도우를 닫는 클로저를 만드는 함수입니다
      function makeOutListener(infowindow) {
        return function() {
          infowindow.close();
        };
      }
    },
    clickAptInfo(house) {
      this.map.setCenter(new kakao.maps.LatLng(house.lat, house.lng));
    },
  },
  computed: {
    ...mapGetters(['getUserSeq']),
  },

  created() {
    if (this.$route.params.select == undefined) this.searchList();
    this.sidoList();
  },
  mounted() {
    this.$store.commit('SET_BREADCRUMB_INFO', {
      isHome: false,
      title: '아파트명 검색',
      subTitle: '동으로 매물 / 거래정보 검색',
      desc: '원하는 지역의 매물정보를 확인해보세요.',
    });
    this.$store.commit('SET_CUR_PAGE', 'searchDong');
  },

  watch: {
    selectedDongCode() {
      http
        .get('/bookmarkArea', {
          params: {
            userSeq: this.getUserSeq,
            dongCode: this.selectedDongCode,
          },
        })
        .then(({ data }) => {
          if (data.result == 'login') {
            router.push('/login');
          } else {
            this.isBookmarked = data;
            console.log('isBookmarked : ' + this.isBookmarked);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    selectedSidoCode() {
      this.gugunList();
    },
    selectedGugunCode() {
      this.dongList();
    },
  },
};
</script>

<style scoped>
#apartInfo {
  height: 550px;
  overflow: auto;
}

#apartInfo::-webkit-scrollbar {
  width: 10px;
}

#apartInfo::-webkit-scrollbar-thumb {
  background-color: rgb(219, 227, 233);
}

#apartInfo::-webkit-scrollbar-track {
  background-color: white;
}

.select {
  width: 200px;
}

.cust_box:hover {
  background-color: #f3b773;
}
</style>
