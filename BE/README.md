### ssh 로 서버 접속

```bash
ssh -i ./sangjun.pem ubuntu@54.248.64.154
```

### mysql DB Create

```bash
create database django_mobile character set utf8mb4 collate utf8mb4_general_ci;
```

뒤에 붙은 인자는 모든 문자를 mysql에 입력할 수 있는 거라는데 그렇게 필요는 없는듯?

### 구니콘 시범 접속

```bash
gunicorn --bind 0.0.0.0:8000 pjtback.wsgi:application
```

### nginx config 수정

```bash
sudo vi /etc/nginx/sites-available/django_test
```

### gunicorn config 수정

```bash
sudo vi /etc/systemd/system/gunicorn.service
```

### 9기 노션으로 바뀌어서 싸피 노션 주소 바뀜

https://hyper-growth.notion.site/SSAFY-Public-Document-9dc94ea8a050472ca00ffe8ea58586da?p=3518928727484a2e840ac15ea06b0864&pm=s

### gunicorn config 양식

```bash
[Unit]
Description=gunicorn daemon
After=network.target

[Service]
User=ubuntu
Group=www-data
WorkingDirectory=/home/ubuntu/mobile_pjt/pjtback
ExecStart=/home/ubuntu/mobile_pjt/pjtback/venv/bin/gunicorn \
        --workers 3 \
        --bind 127.0.0.1:8000 \
        pjtback.wsgi:application

[Install]
WantedBy=multi-user.target
```

### Nginx config 양식

```bash
server {
        listen 80;
        server_name 13.231.102.118;

    location /static/ {
            root /home/ubuntu/mobile_pjt/pjtback/staticfiles/;
    }

    location / {
            include proxy_params;
            proxy_pass http://127.0.0.1:8000;
    }

}
```

### 서버에서 로그 보기

#### Nginx 성공로그 에러로그

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

#### 구니콘로그

```bash
tail -f /var/log/syslog
```

### 전달 받은 DTO

```java
data class Join @JvmOverloads
    constructor(
    val email: String, //아이디
    var pw: String,
    var name: String = "",
    var nickname: String = "",
    var profileImg : String?,
    var age : Int = 0 , //연령대
    val kakao : String = "", //카카오 이메일
    val naver : String = "", //네이버 이메일
    val google : String = "", //구글 이메일
) : java.io.Serializable



data class User @JvmOverloads constructor(
    val uid : Int,
    val join : Join,
   val travel : ArrayList<Travel>,
    val myLikeBoard : ArrayList<Board>,
    val writeBoard : ArrayList<Board>,
    val totalDate : Int,
    val token : Token,
)



data class Travel @JvmOverloads constructor(
    val travelId : String,
    val location : String,
    val startDate : Date,
    val endDate : Date,
    val placeList : ArrayList<Place>
)


data class Place(
    val placeId : Int, //장소 아이디
    val placeName : String, //장소 이름
    val saveDate : Long, //기록 시간
    val memo : String, // 기록
    val placeImgList : ArrayList<String>, //장소 사진 리스트
    val latitude : Double, //위도
    val longitude : Double, //경도
    val address : String, //해당 위치 주소
)


data class Board(
    val boardId: Int, //게시글 번호
    val userId : Int,
    val nickname : String,
    val profileImg : String, //프로필 이미지
    var writeDate : Date, //작성 날짜
    val theme : String, //여행 테마(복수선택 가능)
    val title : String,
    val content: String,
    val imageList : ArrayList<String>,
    val travel : Travel, //여정 정보 객체
    var likeCount : Int = 0, //좋아요 수
    var commentCount : Int = 0, //댓글 수
)


data class Filter @JvmOverloads constructor(
    val themeList : ArrayList<String>, //여행 테마
    val regionList : ArrayList<String>, //여행 지역
    val periodList : ArrayList<String>, //여행 기간
    val ageList : ArrayList<String> //연령대
)


data class Comment @JvmOverloads constructor(
    val commentId : Int, //댓글 아이디
    val boardId : Int, //게시물 아이디
    val profileImg : String,
    val userId : Int, //사용자 아이디
    val nickname : String,
    val content : String,
    val writeDate : Date,
    )
```

### post man 복잡한 json 데이터 raw 로 보내기 예시

```javascript
{
    "title" : "travel 포함",
    "content" : "포함되라 머리 머리",
    "likeCount": 3,
    "commentCount" :4,
    "imageList" : ["adsds", "sdsds"],
    "userId" : 1,
    "travel" : {
        "travelId" : 1,
        "location": "서울",
        "startDate": "2023-01-05 10:55:32",
        "endDate": "2023-01-07 09:40:22",
        "placeList": []
    }

}
```

### 전달받은 필터 항목들

= 테마 = 

- 혼자 여행
- 커플 여행
- 효도 여행
- 우정 여행
- 직장 여행

= 지역 = 

- 강원
- 서울
- 경북*대구
- 경기
- 부산
- 전남*광주
- 제주
- 충남*대전
- 경남
- 충북
- 전북
- 인천

= 여행 기간 = 
당일치기
1박2일
2박3일
3박 4일 
4박5일+  

= 연령대 = 
10대
20대
30대
40대
50대 이상

"서울","경기","강원","부산","경북·대구","전남·광주","제주","충남·대전","경남","충북","경남","전북","인천"

ssh 접근

```bash
ssh -i I8D103T.pem ubuntu@i8d103.p.ssafy.io
```

일단 가장 원시적인 서버 환경에서 부터 시작해서 필요한 명령어들 정리

### 기본 세팅과 docker compose 다운 파트

```bash
sudo apt-get update
sudo apt-get -y dist-upgrade
sudo apt-get install docker
sudo apt-get -y install docker-compose
```

## git clone 파트

```bash
sudo git clone https://lab.ssafy.com/luminaries1/mobile_pjt.git
```

## db , web(gunicorn 실행 명령어 포함), nginx 3개 파트

```bash
sudo docker-compose up --build
```

## certbot 으로 https 붙이는 파트

```bash
sudo snap install core; sudo snap refresh core
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
sudo certbot --nginx
```

## 쉘 스크립트 실행

```bash
chmod +x init-letsencrypt.sh  
./init-letsencrypt.sh
```

## 해결해야 할 점들

* shell script 좀 더 분기점 두거나, git clone 후 dir 이동 하는거 까지 자동으로 하던가 해야할듯?

* 보안적으로 뺄거 좀 다 env 파일 같은걸로 빼야함.. ㅠㅠ 이건 좀 더 데여봐야 알듯?
