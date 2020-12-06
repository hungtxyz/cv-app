import numpy as np
import cv2
from numba import jit
from scipy import ndimage as ndi
from taomask import make_mask

SEAM_COLOR = np.array([0, 0, 255])    # seam visualization color (BGR)
ENERGY_MASK_CONST = 100000.0              # large energy value for protective masking
MASK_THRESHOLD = 254                       # minimum pixel intensity for binary mask
USE_FORWARD_ENERGY = False                 # if True, use forward energy algorithm

# array([[[ 0,  1,  2],  #1 dòng này là 1 pixel, Blue channel là cột đầu của ma trận = [[0,3,6],[9,12,15],[18,21,24]]
#         [ 3,  4,  5],
#         [ 6,  7,  8]],

#        [[ 9, 10, 11],
#         [12, 13, 14],
#         [15, 16, 17]],

#        [[18, 19, 20],
#         [21, 22, 23],
#         [24, 25, 26]]])


#Hiển thị đường SEAM
def visualize(im, boolmask=None, rotate=False): 
    vis = im.astype(np.uint8)
    if boolmask is not None:
        vis[np.where(boolmask == False)] = SEAM_COLOR #lấy ra pixel mang index của đường seam, sau đó gán nó = màu đỏ nổi bật để hiển thị
    if rotate:
        vis = rotate_image(vis, False)
    cv2.imshow("visualization", vis)
    cv2.waitKey(1)
    return vis

#Xoay ảnh
def rotate_image(image, clockwise): 
    k = 1 if clockwise else 3
    return np.rot90(image, k)    

#Tính Gradient Image
def Gradient_Image(im):
    im=im.astype(np.float32)
    img=cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
    output=np.zeros((img.shape[0],img.shape[1]))
    output[:,1:]+=(img[:,1:] - img[:,0:-1])**2 #dx
    output[1:,:]+=(-img[1:,:]+img[0:-1,:])**2 #dy
    return output**0.5

# @jit
# def forward_energy(im):

#     h, w = im.shape[:2]
#     im = cv2.cvtColor(im.astype(np.uint8), cv2.COLOR_BGR2GRAY).astype(np.float64)

#     energy = np.zeros((h, w))
#     m = np.zeros((h, w))
    
#     U = np.roll(im, 1, axis=0)
#     L = np.roll(im, 1, axis=1)
#     R = np.roll(im, -1, axis=1)
    
#     cU = np.abs(R - L)
#     cL = np.abs(U - L) + cU
#     cR = np.abs(U - R) + cU
    
#     for i in range(1, h):
#         mU = m[i-1]
#         mL = np.roll(mU, 1)
#         mR = np.roll(mU, -1)
        
#         mULR = np.array([mU, mL, mR])
#         cULR = np.array([cU[i], cL[i], cR[i]])
#         mULR += cULR

#         argmins = np.argmin(mULR, axis=0)
#         m[i] = np.choose(argmins, mULR)
#         energy[i] = np.choose(argmins, cULR)
#     cv2.imwrite('gradient.jpg',energy)
#     return energy

#Chèn đường seam khi tìm được index
@jit
def add_seam(im, seam_idx): #thêm 1 đường seam vào ảnh có giá trị = trung bình cộng của 2 pixel kế bên

    h, w = im.shape[:2]
    output = np.zeros((h, w + 1, 3)) #thêm 1 đường seam nên chiều rộng +1
    for row in range(h):
        col = seam_idx[row]
        for ch in range(3):
            if col == 0:
                p = int(np.average(im[row, col: col + 2, ch]))
                output[row, col, ch] = im[row, col, ch]
                output[row, col + 1, ch] = p
                output[row, col + 1:, ch] = im[row, col:, ch]
            else:
                p = int(np.average(im[row, col - 1: col + 1, ch]))
                output[row, : col, ch] = im[row, : col, ch]
                output[row, col, ch] = p
                output[row, col + 1:, ch] = im[row, col:, ch]

    return output

@jit
def add_seam_grayscale(im, seam_idx):

    h, w = im.shape[:2]
    output = np.zeros((h, w + 1))
    for row in range(h):
        col = seam_idx[row]
        if col == 0:
            p =int(np.average(im[row, col: col + 2]))
            output[row, col] = im[row, col]
            output[row, col + 1] = p
            output[row, col + 1:] = im[row, col:]
        else:
            p = int(np.average(im[row, col - 1: col + 1]))
            output[row, : col] = im[row, : col]
            output[row, col] = p
            output[row, col + 1:] = im[row, col:]

    return output

#Xóa đường Seam khi tìm được index
@jit
def remove_seam(im, boolmask): #loại bỏ đường SEAM đã tìm được
    h, w = im.shape[:2]
    boolmask3c = np.stack([boolmask] * 3, axis=2) #tạo boolmask thành ma trận 3 chiều 
    return im[boolmask3c].reshape((h, w - 1, 3))

@jit
def remove_seam_grayscale(im, boolmask):
    h, w = im.shape[:2]
    return im[boolmask].reshape((h, w - 1))

#Tìm đường seam nhỏ nhất 
@jit
def get_minimum_seam(im, mask=None, remove_mask=None):
    
    h, w = im.shape[:2]
    energyfn = forward_energy if USE_FORWARD_ENERGY else Gradient_Image
    M = energyfn(im)

    # give removal mask priority over protective mask by using larger negative value
    if remove_mask is not None:
        M[np.where(remove_mask > MASK_THRESHOLD)] = -ENERGY_MASK_CONST * 100 #cho energy vùng đối tượng cần xóa = số rất nhỏ để seam đi qua
    if mask is not None:
        M[np.where(mask > MASK_THRESHOLD)] = ENERGY_MASK_CONST #mask bao quanh vật thể, gán những nơi mask > threshold = 1 gt cao để nó ko xóa vật thể
    backtrack = np.zeros_like(M, dtype=np.int)

    for i in range(1, h):
        for j in range(0, w):
            if j == 0:
                idx = np.argmin(M[i - 1, j:j + 2])
                backtrack[i, j] = idx + j
                min_energy = M[i-1, idx + j]
            else:
                idx = np.argmin(M[i - 1, j - 1:j + 2])
                backtrack[i, j] = idx + j - 1
                min_energy = M[i - 1, idx + j - 1]

            M[i, j] += min_energy

    # backtrack to find path
    seam_idx = []
    boolmask = np.ones((h, w), dtype=np.bool)
    j = np.argmin(M[-1])
    for i in range(h-1, -1, -1):
        boolmask[i, j] = False
        seam_idx.append(j) #lưu lại vị trí đã xóa để lát chèn
        j = backtrack[i, j]

    seam_idx.reverse()
    return np.array(seam_idx), boolmask

#Tìm và xóa đường seam
@jit
def seams_removal(im, num_remove, mask=None, vis=False, rot=False):
    for _ in range(num_remove):
        seam_idx, boolmask = get_minimum_seam(im, mask)
        if vis:
            visualize(im, boolmask, rotate=rot)
        im = remove_seam(im, boolmask)
        if mask is not None:
            mask = remove_seam_grayscale(mask, boolmask)
    return im, mask

#Tìm và chèn đường seam
@jit
def seams_insertion(im, num_add, mask=None, vis=False, rot=False):
    seams_record = []
    temp_im = im.copy()
    temp_mask = mask.copy() if mask is not None else None

    for _ in range(num_add):
        seam_idx, boolmask = get_minimum_seam(temp_im, temp_mask)
        if vis:
            visualize(temp_im, boolmask, rotate=rot)

        seams_record.append(seam_idx)
        temp_im = remove_seam(temp_im, boolmask)
        if temp_mask is not None:
            temp_mask = remove_seam_grayscale(temp_mask, boolmask)

    seams_record.reverse()

    for _ in range(num_add):
        seam = seams_record.pop()
        im = add_seam(im, seam)
        if vis:
            visualize(im, rotate=rot)
        if mask is not None:
            mask = add_seam_grayscale(mask, seam)

        # update the remaining seam indices
        for remaining_seam in seams_record:
            remaining_seam[np.where(remaining_seam >= seam)] += 2         

    return im, mask

#Xóa đối tượng
@jit
def object_removal(im, rmask, mask=None, vis=False, horizontal_removal=False):
    im = im.astype(np.float64)
    rmask = rmask.astype(np.float64)
    if mask is not None: #trong lúc xóa unwanted object thì giữ cái thằng object chính lại chứ k là có thể đường seam sẽ đi qua nó
        mask = mask.astype(np.float64)
    output = im

    h, w = im.shape[:2]

    if horizontal_removal: #Xóa theo từng hàng
        output = rotate_image(output, True)
        rmask = rotate_image(rmask, True)
        if mask is not None:
            mask = rotate_image(mask, True)

    while len(np.where(rmask > MASK_THRESHOLD)[0]) > 0:#ở đây np.where() sẽ trả về 2 array (array idex của hàng, array index của cột)
        #ta sẽ thực hiện đến khi nào trong rmask không còn pixel cần xóa nữa thì thôi (các giá trị trong rmask <threshold)
        seam_idx, boolmask = get_minimum_seam(output, mask, rmask)
        if vis:
            visualize(output, boolmask, rotate=horizontal_removal)            
        output = remove_seam(output, boolmask) # loại bỏ đường seam trong ảnh RGB
        rmask = remove_seam_grayscale(rmask, boolmask) # Đồng thời loại bỏ đường seam tương ứng trong Remove Mask
        if mask is not None:
            mask = remove_seam_grayscale(mask, boolmask)

    num_add = (h if horizontal_removal else w) - output.shape[1]#nếu xóa hàng thì ta add thêm hàng, xóa cột thì add thêm cột 
    #vì khi lật ngược ảnh để xóa hàng thì output.shape[1] hiện tại đang là số hàng
    #nếu xóa cột thì output.shape[1] đang là số cột
    output, mask = seams_insertion(output, num_add, mask, vis, rot=horizontal_removal)
    if horizontal_removal:
        output = rotate_image(output, False)

    return output        

# def process(path):
#     img=cv2.imread(path)
#     rmask=make_mask(img)
#     result=object_removal(img,rmask,vis=True)
#     cv2.imwrite('result.jpg',result)
#     return result

# if __name__ == '__main__':
#     path=input()
#     img=cv2.imread(path)
#     protect=make_mask(cv2.imread('protect.png'))
#     # cv2.imwrite('protect_mask.jpg',protect)
#     rmask=make_mask(img)
#     result=object_removal(img,rmask,mask=protect,vis=True)
#     #result=object_removal(img,rmask,vis=True,horizontal_removal=True)
#     cv2.imwrite('result.png',result)

