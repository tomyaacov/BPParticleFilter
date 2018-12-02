% one hot encoding
observation_classes = unique(XTrain{1});
XTrain_enc = {};
for i = 1:length(observation_classes)
    for j = 1:length(XTrain)
        XTrain_enc{j}(i,:) = double(XTrain{j} == observation_classes(i));
    end
end
XTest_enc = {};
for i = 1:length(observation_classes)
    for j = 1:length(XTest)
        XTest_enc{j}(i,:) = double(XTest{j} == observation_classes(i));
    end
end

% creating categorical vector for target
classes = unique([YTrain{1:num_train_samples}]);
YTrain_enc = YTrain;
for j = 1:length(XTrain)
        YTrain_enc{j} = categorical(state_names(YTrain_enc{j}));
end

YTest_enc = YTest;
for j = 1:length(XTrain)
        YTest_enc{j} = categorical(state_names(YTest_enc{j}));
end


% visualization - need to adapt to data
% X = XTrain_enc{1}(1,:);
% classes = unique([YTrain{1:num_train_samples}]);
% 
% figure
% for j = 1:numel(classes)
%     label = classes(j);
%     idx = find(YTrain{1} == label);
%     hold on
%     plot(idx,X(idx))
% end
% hold off
% 
% xlabel("Time Step")
% ylabel("Acceleration")
% title("Training Sequence 1, Feature 1")
% legend(classes,'Location','northwest')

%configuring the network
featureDimension = 3;
numHiddenUnits = 100;
numClasses = length(classes);

layers = [sequenceInputLayer(featureDimension)
    lstmLayer(numHiddenUnits,'OutputMode','sequence')
    fullyConnectedLayer(numClasses)
    softmaxLayer
    classificationLayer];


options = trainingOptions('sgdm', ...
    'LearnRateSchedule','piecewise', ...
    'LearnRateDropFactor',0.2, ...
    'LearnRateDropPeriod',5, ...
    'MaxEpochs',20, ...
    'MiniBatchSize',64, ...
    'Plots','training-progress');

overall_states = 0;
overall_accurate_estimations = 0;
tic
%training the network
net = trainNetwork(XTrain_enc,YTrain_enc,layers,options);

% visualization - need to adapt to data
%load HumanActivityTest
% figure
% plot(XTest')
% xlabel("Time Step")
% legend("Feature " + (1:featureDimension))
% title("Test Data")

YPred = classify(net,XTest_enc);

% doesnt work
% acc = sum(YPred == YTest_enc)./numel(YTest);

for i = 1:length(XTest)
    overall_accurate_estimations = overall_accurate_estimations + sum(YPred{i} == YTest_enc{i});
    overall_states = overall_states + length(YTest_enc{i});
end

time_elapsed_lstm = toc;

accuracy_lstm = overall_accurate_estimations/overall_states;

fprintf('accuracy_lstm: %f , time_elapsed_lstm: %f sec.\n',accuracy_lstm,time_elapsed_lstm);

% visualization - need to adapt to data
% figure
% plot(YPred,'.-')
% hold on
% plot(YTest)
% hold off
% 
% xlabel("Time Step")
% ylabel("Activity")
% title("Predicted Activities")
% legend(["Predicted" "Test Data"])



