clear all;
close all;
clc;

%Jeremy Bredfeldt

%create a 3D collagen fiber object space

%Create a dense fiber network test case. 
    %Many fibers in parallel.
    %Many small fibers near eachother.

%Create a test case of curvy fibers that are random and another case with straight fibers that
%are random. Use automated analysis to distinguish between the two. In 2D
%and 3D.

%would like at the end:
% average angle of each fiber
% angle between start and end point of each fiber
% how many times the fiber crossed the line connecting the end points for
%  each fiber
% angles of all fiber segments in image
% stdev of angles in each fiber
% entropy of the angles in each fiber
% distance along each fiber
% distance between end points of each fiber


w = 1024; %pixels % ym: -> 512
l = w;   %pixels
h = 30;   %pixels

ps = 0.1E-6; %m/pixel = pixel size
image_width_in_microns = w*ps*1e6

n = 10;%  %number of fibers ym: 100->
pct_strght = 0.30;

% dir_name = sprintf('Z:\\liu372\\fiberextraction\\testimages\\simulatedimages\\nf%d_ps%4.2e_3',n,ps);
% %dir_name = sprintf('E:\\Images\\SyntheticData\\CollagenMatrices\\nf%d_ps%4.2e_3',n,ps);
% mkdir(dir_name);
dir_name = sprintf('%s\\nf%d_ps%4.2e',pwd,n,ps);
if ~exist(dir_name,'dir')
    mkdir(dir_name);
end


total_t = 0;


%create output structure
fiber_info(n) = struct('diam', [], 'inten', [], 'max_len', [], ...
                       'actual_len', [], 'end_pt_dist', [], ...
                       'end_pt_az', [], 'end_pt_inc', [], ...
                       'all_az', [], 'all_inc', [], ...
                       'pos_x', [], 'pos_y', [], 'pos_z', []);

for cases = 1%35:39
%test volume
tv = zeros(w,l,h);    

rng(1001);
WIDall = round(10*rand(n,1))+4; % generate all the diameters
LENall = nan(n,1);
%loop through each fiber
for j = 1:n
    
    d = WIDall(j,1); 
    %d = 4;
    if (mod(d,2) == 0)
        d = d*10^-7; %fiber diameter (m)
    else
        d = (d+1)*10^-7;
    end
    dp = d/ps; %fiber diameter (pixels not rounded)
    dp2 = dp/2; %fiber radius (pixels not rounded)
    dpr = round(dp); %integer number of pixels for fiber diameter
    dpr2 = round(dp2); %integer number of pixels for fiber radius
    %r_step = dp2/2; %pixels
    r_step = 1;
    
    %average fiber length (in pixels)
    %avg_fib_len = 10E-6/ps;
    %max_fib_len = 100; %in steps  % ym: 400->
    rn = randn;
    max_fib_len = rn*rn*25+20;

    m = w/2; %midpoint of volume, fiber centered here

    x = 1:w;
    y = 1:l;
    z = 1:h;
    zr = zeros(w,l,dpr);

    xr = repmat(x,[w,1,dpr]);
    yr = repmat(y',[1,l,dpr]);
    for i = 1:dpr
        zr(:,:,i) = i;
    end
    r = zeros(w,l,dpr);

    t = cputime;

    %loop through each position in each fiber
    pos_x = rand*w; %starting position (in pixels)
    pos_y = rand*l;
    pos_z = h/2;
    %pos_x = w/2;
    %pos_y = l/2;
    %pos_z = h/2;
    
    if j == 2
        rstep = rstep*2;
    else
        rstep = dp2/2;
    end
    
    %az_ang = (rand-0.5)*2*pi; %starting azimuth angle (random in any
    %direction)
    if j < n*(1-2*pct_strght);
        az_ang = (rand-0.5)*pi; %starting azimuth angle
    elseif j < n*(1-pct_strght)
        az_ang = -30*pi/180;
    else
        az_ang = -105*pi/180;
    end
    inc_ang = pi/2; %(rand-0.5)*pi; %starting inclination angle (keep fiber in the x-y plane)
    
    escaped = 0;
    at_end = 0;
    num_pix = 0;
    rpos_x_prev = 0;
    rpos_y_prev = 0;
    rpos_z_prev = 0;
    
    %store starting positions
    st_pos_x = pos_x;
    st_pos_y = pos_y;
    st_pos_z = pos_z;
    
    fib_intensity = 0.25*rand + 0.75;
    
    
    %Build fiber position array
    while (escaped == 0 && at_end == 0)
        x_inc = r_step*sin(inc_ang)*cos(az_ang);
        y_inc = r_step*sin(inc_ang)*sin(az_ang);
        %z_inc = r_step*cos(inc_ang);
        z_inc = 0;
        pos_x = pos_x + x_inc; %do not round the positions here, want to accumulate fractions of a pixel
        pos_y = pos_y + y_inc;
        pos_z = pos_z + z_inc;

        %get rounded positions here
        rpos_x = round(pos_x);
        rpos_y = round(pos_y);
        rpos_z = round(pos_z);

        %check if we are still within image volume
        if (rpos_x >= w || rpos_y >= l || rpos_z >= h-dp2 || rpos_x < 1 || rpos_y < 1 || rpos_z < 1+dp2)
            escaped = 1;
            break;
        end
        if (num_pix > max_fib_len)
            at_end = 1;
            break;
        end  

        %get new trajectory (in radians)
        if j < n*(1-2*pct_strght);
            az_ang = mod(az_ang + (rand-0.5)*pi/32,2*pi);
        else
            az_ang = az_ang;
        end
        %inc_ang = mod(inc_ang + (rand-0.5)*pi/8,pi);
        %az_ang = pi/4; %az_ang + az_inc;
        %inc_ang = 0;% inc_ang + inc_inc;
        %az_ang = pi/2;
        inc_ang = pi/2; %fixed inclination angle
        
        
        
        %put a white sphere in the test volume
        r = ((xr-rpos_x).^2 + (yr-rpos_y).^2 + (zr-dpr2).^2)<dp2^2;
        %tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1) = fib_intensity*((tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1) + r)>0);
        %tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1) = ((tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1) + fib_intensity*r));
        tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1) = max(tv(:,:,rpos_z-dpr2:rpos_z+dpr2-1),fib_intensity*r);
        
        %record some metrics (only if we have moved to a new voxel)
        if (rpos_x ~= rpos_x_prev || rpos_y ~= rpos_y_prev || rpos_z ~= rpos_z_prev)
            num_pix = num_pix + 1;
            
            fiber_info(j).all_az(num_pix) = (180/pi)*az_ang; %in degrees
            fiber_info(j).all_inc(num_pix) = (180/pi)*inc_ang; %in degrees
            
            fiber_info(j).pos_x(num_pix) = pos_x;
            fiber_info(j).pos_y(num_pix) = pos_y;
            fiber_info(j).pos_z(num_pix) = pos_z;
        end
    end
    
    %store results about the fibers
    fiber_info(j).diam = d;
    fiber_info(j).inten = fib_intensity;
    fiber_info(j).max_len = max_fib_len;
    fiber_info(j).actual_len = num_pix; %this is the actual number of pixels occupied by the fiber
    fiber_info(j).end_pt_dist = sqrt((pos_x - st_pos_x).^2 + (pos_y - st_pos_y).^2 + (pos_z - st_pos_z).^2);
    fiber_info(j).end_pt_az = -1*(180/pi)*atan((pos_y - st_pos_y)/(pos_x - st_pos_x)); %in degrees
    fiber_info(j).end_pt_inc = (180/pi)*acos((pos_z - st_pos_z)/num_pix); %in degrees
    
    LENall(j,1) = fiber_info(j).actual_len; % fiber length
    
    
    fibers_finished = j
    elapsed_t = cputime-t
    total_t = total_t + elapsed_t
    t = cputime;
   
end

%%
%write out volume to an image stack
  str = sprintf('%s\\case%d_nf%d_ps%4.2e.tif',dir_name,cases,n,ps);
  str_fib_inf = sprintf('%s\\case%d_nf%d_ps%4.2e.mat',dir_name,cases,n,ps);  
  save(str_fib_inf,'fiber_info','n','ps');
  
  WIDLEN = [WIDall,LENall];
  str_fib_inf2 = sprintf('%s\\case%d_nf%d_ps%4.2e.xlsx',dir_name,cases,n,ps);  
  xlswrite(str_fib_inf2,WIDLEN)
  
%for k = 1:h
for k = round(h/2):round(h/2)
%      str = sprintf('%s\\image%03d.tif',dir_name,k-1);
%      imwrite(uint8(tv(:,:,k)*255),str,'tiff');
        noise = randn(w,l).^2;
        noise = noise/max(max(noise));
        
        max_tv = max(max(tv(:,:,k)));

        imwrite(uint8((tv(:,:,k)*255/max_tv)),str,'tiff','WriteMode','append','Compression','none');

end

end

fclose all;


