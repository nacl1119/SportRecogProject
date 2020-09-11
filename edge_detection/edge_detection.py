import numpy as np
import timeit
import cv2

def edge_detection(color_img):
    start_time = timeit.default_timer()  # 시간 측정 시작

    '이미지 블러링'
    color_img = cv2.GaussianBlur(color_img, (3, 3), 0)

    M, N, D = color_img.shape

    if max(M, N) > 1500: # 행이나 열이 1500px보다 큰 경우 행과 열 모두 반으로 줄임.
        color_img = cv2.resize(color_img, None, fx=0.5, fy=0.5, interpolation=cv2.INTER_AREA)

    if M > N: # 행이 열보다 길면 오른쪽으로 회전
        color_img = cv2.rotate(color_img, cv2.ROTATE_90_CLOCKWISE)

    #cv2.imshow('color', color_img)


    '엣지 검출'
    edge_x = cv2.Sobel(color_img, cv2.CV_64F, 1, 0, ksize=3)
    edge_x = cv2.convertScaleAbs(edge_x)
    edge_y = cv2.Sobel(color_img, cv2.CV_64F, 0, 1, ksize=3)
    edge_y = cv2.convertScaleAbs(edge_y)
    edge = cv2.addWeighted(edge_x, 0.5, edge_y, 0.5, 0);
    #cv2.imshow('raw_edge', edge)


    '엣지에서 노이즈 제거'
    edge = cv2.fastNlMeansDenoisingColored(edge, None, 20, 20, 15, 25)
    #cv2.imshow('denoised_edge', edge)


    '남은 엣지 날카롭게'
    sharpening = np.array([[-1, -1, -1], [-1, 9, -1], [-1, -1, -1]])
    edge = cv2.filter2D(edge, -1, sharpening)
    #cv2.imshow('sharpened_denoised_edge', edge)

    '컬러 -> 흑백'
    edge = cv2.cvtColor(edge, cv2.COLOR_BGR2GRAY)

    '다시 한번 노이즈 제거'
    edge[edge <= 30] = edge[edge <= 30]*0.1
    #cv2.imshow('denoised_sharpened_denoised_edge', edge)

    '흑백 전환'
    edge = 255-edge
    edge = np.uint8(edge)


    terminate_time = timeit.default_timer() # 시간 측정 종료
    print("{}초 소요".format(terminate_time - start_time))

    #cv2.imshow('final edge', edge)
    #cv2.waitKey(0)
    #cv2.destroyAllWindows()

    return edge


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--img', type=str, default='test5.jpg')
    args = parser.parse_args()

    color_img = cv2.imread(args.img, cv2.IMREAD_COLOR) # 이미지 읽기
    edge = edge_detection(color_img) # 엣지 검출
    cv2.imwrite('edge.jpg', edge) # 검출된 엣지 이미지 저장
