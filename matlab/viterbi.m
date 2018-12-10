tic
overall_states = 0;
overall_accurate_estimations = 0;
estimated_states_viterbi = {};

for i = 1:length(XTest)
    estimated_states_viterbi{i} = hmmviterbi(XTest{i},trans,emis);
    overall_accurate_estimations = overall_accurate_estimations + sum(estimated_states_viterbi{i} == YTest{i});
    overall_states = overall_states + length(states);
end

time_elapsed_viterbi = toc;

accuracy_viterbi = overall_accurate_estimations/overall_states;

fprintf('accuracy_viterbi: %f , time_elapsed_viterbi: %f sec.\n',accuracy_viterbi,time_elapsed_viterbi);
 
 
 
 