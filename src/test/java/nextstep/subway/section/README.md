#4단계 - 구간 제거 기능
##요구사항
###기능 요구사항
- 요구사항 설명에서 제공되는 요구사항을 기반으로 지하철 구간 제거 기능을 구현하세요.
- 요구사항을 정의한 인수 조건을 조출하세요.
- 인수 조건을 검증하는 인수 테스트를 작성하세요.
- 예외 케이스에 대한 검증도 포함하세요.

###프로그래밍 요구사항
- 인수 테스트 주도 개발 프로세스에 맞춰서 기능을 구현하세요.
  - 요구사항 설명을 참고하여 인수 조건을 정의
  - 인수 조건을 검증하는 인수 테스트 작성
  - 인수 테스트를 충족하는 기능 구현
- 인수 조건은 인수 테스트 메서드 상단에 주석으로 작성하세요.
  - 뼈대 코드의 인수 테스트를 참고
- 인수 테스트의 결과가 다른 인수 테스트에 영향을 끼치지 않도록 인수 테스트를 서로 격리 시키세요.
  - 인수 테스트의 재사용성과 가독성, 그리고 빠른 테스트 의도 파악을 위해 인수 테스트를 리팩터링 하세요.

##요구사항 설명

###API 명세

###지하철 구간 삭제 request
```http request
DELETE /lines/1/sections?stationId=2 HTTP/1.1
accept: */*
host: localhost:52165
```
### 노선의 구간을 제거하는 기능을 구현하기
- 종점이 제거될 경우 다음으로 오던 역이 종점이 됨
- 중간역이 제거될 경우 재배치를 함
  - 노선에 A - B - C 역이 연결되어 있을 때 B역을 제거할 경우 A - C로 재배치 됨
  - 거리는 두 구간의 거리의 합으로 정함

###구간 삭제 시 예외 케이스를 고려하기
- 기능 설명을 참고하여 예외가 발생할 수 있는 경우를 검증할 수 있는 인수 테스트를 만들고 이를 성공 시키세요.
>예시) 노선에 등록되어있지 않은 역을 제거하려 한다.

- 구간이 하나인 노선에서 마지막 구간을 제거할 때
  - 제거할 수 없음

###구간 제거
구간 제거 요청 처리
```java
@DeleteMapping("/{lineId}/sections")
public ResponseEntity removeLineStation(
@PathVariable Long lineId,
@RequestParam Long stationId) {
    lineService.removeSectionByStationId(lineId, stationId);
    return ResponseEntity.ok().build();
}
```
---
## 지하철역 삭제 인수 테스트
### 일반적인 케이스
- given 세개의 지하철이 노선에 등록되어 있다.
- 가장 앞쪽의 지하철 역 삭제 하는 경우
  - [x] when 지하철 노선에 지하철 역 삭제 요청
  - [x] then 지하철역이 삭제 된다.

- 가장 뒤쪽의 지하철 역 삭제 하는 경우
    - [x] when 지하철 노선에 지하철 역 삭제 요청
    - [x] then 지하철역이 삭제 된다.

- 가운데의 지하철 역 삭제 하는 경우
    - [x] when 지하철 노선에 지하철 역 삭제 요청
    - [x] then 지하철역이 삭제 된다.

### 얘외 케이스
- 노선에 없는 역을 삭제 하는 경우
  - [x] when 지하철 노선에 지하철 역 삭제 요청
  - [x] then 지하철역이 삭제 실패 한다.
  
- 구간이 하나인 노선에서 지하철역을 삭제 하는 경우
  - [x] when 지하철 노선에 지하철 역 삭제 요청
  - [x] then 지하철역이 삭제 실패 한다.

#3단계 - 구간 추가 기능

##요구사항
###기능 요구사항
- 요구사항 설명에서 제공되는 요구사항을 기반으로 지하철 구간 추가 기능을 구현하세요.
- 요구사항을 정의한 인수 조건을 조출하세요.
- 인수 조건을 검증하는 인수 테스트를 작성하세요.
- 예외 케이스에 대한 검증도 포함하세요.
###프로그래밍 요구사항
- 인수 테스트 주도 개발 프로세스에 맞춰서 기능을 구현하세요.
  - 요구사항 설명을 참고하여 인수 조건을 정의
    - 인수 조건을 검증하는 인수 테스트 작성 
    - 인수 테스트를 충족하는 기능 구현 
- 인수 조건은 인수 테스트 메서드 상단에 주석으로 작성하세요. 
    - 뼈대 코드의 인수 테스트를 참고 
- 인수 테스트의 결과가 다른 인수 테스트에 영향을 끼치지 않도록 인수 테스트를 서로 격리 시키세요.
- 인수 테스트의 재사용성과 가독성, 그리고 빠른 테스트 의도 파악을 위해 인수 테스트를 리팩터링 하세요.


## 요구사항 설명

---
### API 명세
- 구간 등록 API Request
```http
POST /lines/1/sections HTTP/1.1
accept: */*
content-type: application/json; charset=UTF-8
host: localhost:52165

{
    "downStationId": "4",
    "upStationId": "2",
    "distance": 10
}
```
---
## 구간 등록 인수 테스트

### 일반적인 케이스
  - 새로운 역을 상,하행 종점으로 등록할 경우
    - [x] when 지하철 노션에 구간 등록 요청
    - [x] then 지하철 노선에 구간 등록 된다.
    
  - 역 사이에 새로운 역을 등록할 경우
    - [x] when 지하철 노션에 지하철 역사이에 구간 등록 요청
    - [x] then 지하철 노선에 구간 등록 된다

### 예외 케이스
  - [x] 역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 등록을 할 수 없음
      - [x] given 노선에 길이가 10인 구간을 등록 한다.
      - [x] when 노선에 길이가 10인 새로운 구간을 등록 한다.
      - [x] then 등록 할 수 없다.
  
  - [x] 상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가할 수 없음
    - [x] given 노선에 구간을 등록 한다.
    - [x] when 노선에 기존의 구간에 등록된 상행역과 하행역을 가진 새로운 구간을 등록한다.
    - [x] then 등록 할 수 없다.
    - 
  - [x] 상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가할 수 없음
    - [x] given 노선에 표함되어 있지 않는 새로운 지하철 역을 등록 한다.
    - [x] when 노선에 기존의 구간에 등록된 상행역과 하행역이 둘다 없는 구간을 등록한다.
    - [x] then 등록 할 수 없다.
  - 

## 구현 목록
- [x] SectionRequest dto
- [x] Section 엔티티 매핑
- [x] Line 엔티티와  Section 엔티티의 연관관계 매핑
  - [x] 연관관계 편의 메소드
- [x] LineController에 구간 추가 api 
- [x] Line -> Section 으로 이동한 property에 따른 LineRequest, LineResponse dto 변경