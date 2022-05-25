# 1단계 - 지하철역 인수 테스트 작성

## 요구사항

---

## 기능 요구사항
- 지하철역 인수 테스트를 완성하세요.
- [x] 지하철역 목록 조회 인수 테스트 작성하기
- [x] 지하철역 삭제 인수 테스트 작성하기

## 프로그래밍 요구사항
- 인수 테스트의 재사용성과 가독성, 그리고 빠른 테스트 의도 파악을 위해 인수 테스트를 리팩터링 하세요.

>각각의 테스트를 동작시키면 잘 동작하지만 한번에 동작시키면 실패할 수 있습니다. 이번 단계에서는 이 부분에 대해 고려하지 말고 각각의 인수 테스트를 작성하는 것에 집중해서 진행하세요.

- [x] 지하철역을 생성한다.
  - [x] When 지하철역을 생성하면
  - [x] Then 지하철역이 생성된다
  - [x] Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다.
- [x] 기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.
  - [x] Given 지하철역을 생성하고
  - [x] When 기존에 존재하는 지하철역 이름으로 지하철역을 생성하면
  - [x] Then 지하철역 생성이 안된다
- [x] 2개의 지하철역을 생성 조회한다.
  - [x] Given 2개의 지하철역을 생성하고
  - [x] When 지하철역 목록을 조회하면
  - [x] Then 2개의 지하철역을 응답 받는다
- [x] 지하철역을 제거한다.
  - [x] Given 지하철역을 생성하고
  - [x] When 그 지하철역을 삭제하면
  - [x] Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
