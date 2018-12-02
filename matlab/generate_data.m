trans = csvread('hmm_transition_matrix.csv');
emis = csvread('hmm_observations_emission.csv');
 
state_names = {'GoodGoodGood', 'GoodGoodFault1', 'GoodGoodFault2', 'GoodGoodFault3', 'GoodFault1Good', 'GoodFault1Fault1', 'GoodFault1Fault2', 'GoodFault1Fault3', 'GoodFault2Good', 'GoodFault2Fault1', 'GoodFault2Fault2', 'GoodFault2Fault3', 'GoodFault3Good', 'GoodFault3Fault1', 'GoodFault3Fault2', 'GoodFault3Fault3'};
observation_names = {'Hot', 'Cold', 'Normal'};
 
%[seq,states] = hmmgenerate(100,trans,emis,'Statenames',state_names,'Symbols',observation_names);
%estimated_states = hmmviterbi(seq,trans,emis,'Statenames',state_names,'Symbols',observation_names);

seq_len = 2000;
num_train_samples = 20;
num_test_samples = 20;
XTrain = {};
YTrain = {};
XTest = {};
YTest = {};
for i = 1:num_train_samples
    [seq,states] = hmmgenerate(seq_len,trans,emis);
    XTrain(i) = {seq};
    YTrain(i) = {states};
end
for i = 1:num_test_samples
    [seq,states] = hmmgenerate(seq_len,trans,emis);
    XTest(i) = {seq};
    YTest(i) = {states};
end


