%read .json file from Matlab

clear, clc, home, close all

jsonFile = './synSize512-fiber100_data.json';
imageFile = './synSize512-fiber100.png';
% jsonFile = './synSize2048-fiber500_data.json';
% imageFile = './synSize2048-fiber500.png';

% % get the image informaiton
% infoImage = imfinfo(imageFile);

fid = fopen(jsonFile); 
rawData = fread(fid,inf); 
stringData = char(rawData'); 
fclose(fid); 
synfibersData = jsondecode(stringData);
fiberNumber = size(synfibersData.fibers,1);
%% read x, y of each fiber from synfibersData
for i = 1%: fiberNumber
    pointsFiber = size(synfibersData.fibers(i).points,1);
    % intialize x y vector for each fiber 
    xV = nan(pointsFiber,1);
    yV = nan(pointsFiber,1);
    for j = 1:pointsFiber
       xV(j) = synfibersData.fibers(i).points(j).x;
       yV(j) = synfibersData.fibers(i).points(j).y;
    end
    %plot this fiber on original image
    figure('pos',[100 200 800 800])
    imshow(imageFile);
    hold on
    pointsShow = [1:5:pointsFiber];
    plot(xV(pointsShow),yV(pointsShow),'r.')
    axis ij
    hold off 
end