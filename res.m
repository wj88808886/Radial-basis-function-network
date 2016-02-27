M = csvread('results.csv');
O = csvread('origin.csv');
x = M(:,1);
y = M(:,2);
z = M(:,3);
xs = O(:,1);
ys = O(:,2);

scatter(xs,ys, 'g' );
hold;
plot(x, y,'r');
plot(x, z, '-');
hold;