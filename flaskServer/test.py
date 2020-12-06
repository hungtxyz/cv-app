import cv2
from seam_carving import *

img = cv2.imread("./origin.jpg")
mask = cv2.imread("./mask.jpg", 0)
result = object_removal(img, mask, vis=True)
cv2.imwrite('result.jpg', result)

