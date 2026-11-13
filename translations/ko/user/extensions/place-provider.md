---
label: 장소 제공자
order: 5
---

# 장소 제공자

모델이 장소를 지어내는 대신 실제 지구의 장소를 찾아볼 수 있게 합니다. 도시, 국가, 바다와 만, 강, 호수,
섬, 산, 사막, 항구와 공항을 좌표와 소속 국가와 함께 다룹니다. 또한 어떤 장소 주변에 무엇이 있는지, 두 장소가 얼마나 떨어져 있는지도 알려 주므로, 실제 지도 위에 놓인 이야기가 거리와 이웃 관계를 올바르게 갖게 됩니다.

이 확장 기능에는 자체 화면이 없고 설정할 것도 없습니다. [Lore Generator](lore-generator.md#tools)와 [Arc Outline](arc-outline.md)은 **도구** 버튼([모델을 위한 도구](tools.md))의 *Place provider* 도구 모음으로 이것을 모델에 제공하며, 켜거나 끌 수 있습니다. 또한 [에이전트](../agents.md)와 명령줄을 위한 명령으로도 제공됩니다.

<a id="what-it-knows"></a>

## 무엇을 알고 있나

데이터는 [Natural Earth](https://www.naturalearthdata.com/)(퍼블릭 도메인)에서 가져온 것으로, **주목할 만한** 장소들의 지도입니다. 약 18 000개가 있으며, 그중 도시 4 900개, 국가 250개, 주와 도 4 500개, 강 1 100개, 호수 500개, 섬 400개, 산과 산맥 350개, 항구 1 000개, 공항 900개입니다. 이것은 지명 사전이 아닙니다. 작은 개울이나 마을은 들어 있지 않습니다. Natural Earth는 대부분의 작은 만(inlet)도 bay로 그리기 때문에, 캐나다의 inlet을 요청하면 이 데이터가 가진 네 곳(Hamilton Inlet, Bathurst Inlet, Minto Inlet, Richard Collinson Inlet)이 나오며, 브리티시컬럼비아 해안의 수백 곳이 나오지는 않습니다.

<a id="commands"></a>

## 명령

모든 명령은 읽기만 합니다.

| 명령 | 하는 일 |
|---|---|
| `/list-place-types` | 장소의 유형(`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...)과 각 유형의 개수를 나열합니다. |
| `/search-places query [type [country [limit]]]` | 이름, 유형, 국가로 검색하며, 가장 잘 맞는 결과를 먼저 보여 줍니다. 질의로 `*`를 쓰면 모든 이름을 뜻합니다. `/search-places * inlets Canada`는 캐나다의 inlet을 모두 나열합니다. |
| `/places-near place radius_km [type [limit]]` | 장소 또는 지점에서 반경 안에 있는 장소를, 가까운 순서로, 킬로미터 단위의 거리와 방향과 함께 보여 줍니다. |
| `/distance-between from to` | 지구 위의 직선 거리(도로가 아님)를 킬로미터와 마일로, 방향과 함께 보여 줍니다. |

*장소*는 이름(`Ottawa`; 같은 이름이 여럿이면 `Paris, France`처럼 쉼표 뒤에 국가나 주를 적어 하나를 고릅니다) 또는 좌표(`45.42, -75.69`)로 적습니다. *유형*은 복수형으로 쓰거나 사람들이 말하는 방식대로 쓸 수 있습니다. 예: `inlets`, `mountains`, `towns`, `states`. *국가*는 이름 또는 코드로 적습니다. 예: `Canada`, `Czechia`, `CA`, `CAN`. 이름은 대소문자와 악센트를 구분하지 않고 비교합니다. 최대 100개의 장소가 반환되며(제한을 주지 않으면 25개), 답변에는 전체 해당 개수가 함께 나옵니다.

유형으로 검색하면, 이름에 그 단어가 들어 있는 비슷한 수역 유형의 장소도 찾습니다. 예를 들어 `inlet`으로 검색하면 Natural Earth가 bay로 분류한 Hamilton Inlet도 찾습니다.

<a id="using-it-from-an-agent"></a>

## 에이전트에서 사용하기

명령을 허용받은 에이전트(*All non-destructive* 선택 항목과 *Auditor* 역할에 포함됨)는 이를 함수로 호출할 수 있습니다. 예를 들어 "첫 장을 노르웨이의 피오르 위 어촌, 수도에서 배로 이틀 거리에 놓아 줘"와 같은 프롬프트를 위해 만들어졌습니다. 이때 에이전트는 `fjords of Norway`를 검색하고, 하나를 고른 뒤, 오슬로까지의 거리를 묻습니다.

<a id="usage"></a>

## 사용법

*Admin → Extensions*에서 확장 기능을 불러옵니다. Lore Generator와 Arc Outline에서 **도구**의 Place provider 항목 중 *이 도구 모음 사용*을 체크합니다. 그러면 그 힌트가 모델에게 위치나 거리를 추측하는 대신 실제 장소를 찾아보라고 알려 줍니다.
