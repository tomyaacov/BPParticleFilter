csvfiles = dir('data_20_11_18/*.csv');
Train = {};
Test = {};
Data = cell(1000,1000);
i = 1;
for file = 0:999
    tmp = readtable(join(['/Users/tomyaacov/Desktop/university/thesis/BPParticleFilter/data_20_11_18', '/id_', int2str(file),'.csv']));
    Data(1) = tmp.Event;
    i = i+1;
end


