# SportRecogProject

재밌게 해봅시다용 -> return "예압" 

### 참고자료

1. 텐서플로 모델 안드로이드에서 사용하기  
*link : https://pythonkim.tistory.com/131?category=703510* -> return "오 좋은 링크."

2. pb to tflite  
*link : https://ebbnflow.tistory.com/172* -> return "h5를 tflite로 변환 가능하다면 쉽게 되겠는걸."

3. 안드로이드에서 tflite 로드하고, 모델 실행하기  
*link : https://stackoverflow.com/questions/60916714/problem-decoding-the-output-of-a-tflite-model-in-android*

### 아이디어

1. 데이터 수집 관련: 그러고 보니 트래핑 음성 데이터 수집은 유튜브에서 따오면 될 듯. ㅋㅋ 트래핑 잘하시는 분들 영상에서 따올게. ㅎㅎ 일단은 굳이 가서 녹음할 필요 없어보임. 나중에 필요하다면 직접 추가로 녹음을 해야하겠지만. : 좋습니다용 ㅎㅎ 

2. 데이터 수집 관련 new: 유튜브에서 따온 볼 리프팅 음성을 teachable machine에 upload하려고 하니까, teachable machine을 통해 얻은 음성 파일이 아니면 안 된다고 뜨네. 아마도 teachable machine에 맞게 전처리가 되 있는 음성 파일만 되는듯. 직접 녹음하는 수밖에 없어보임. ㅋㅋ 이번주에 제천 내려가니 집에서 트래핑하면서 해봐야할듯. 

3. 접근법에 관한 고민: 음성 인식으로 접근하는 것이 쉬울까? 아니면 영상 인식으로 접근하는 것이 쉬울까? YOLO 같은 객체검출 알고리즘 활용해서 검출된 공과 신발 사이의 거리를 가지고 리프팅의 횟수를 카운트할 수도 있을 것 같다는 생각이 들어서. 그런데 이렇게 하면 공과 신발만 검출할 수 있게 직접 객체 검출 모델을 훈련시킬 필요가 있으니 일이 꽤 커짐. ㅋㅋ
