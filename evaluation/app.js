angular.module('evaluation', ['angular-flot']) .controller('EvalCtrl', function($scope, $http) {
    $scope.graphList = ['cpa', 'graph10', 'graph20', 'graph30', 'graph40', 'graph50', 'graph60', 'graph70', 'graph80', 'graph90', 'graph100', 'interp_dil', 'ivd', 'mod_lib', 'pcr', 'pdna'];
    $scope.chartOptions = {
        series: {
            lines: { show: true },
            points: { show: true }
        },
        grid: {
            clickable: true,
            hoverable: true
        }
    };
    $scope.dataset = [];
    $scope.results = [];

    $scope.queryData = function() {
        $scope.showSolution = false;
        $http.get('results/' + $scope.graph + '.txt_results.json').then(function(response) {
            $scope.results = response.data;
            $scope.setData(response.data);
        }, function() {
            $scope.results = [];
            $scope.dataset = [];
        })
    };

    $scope.setData = function(data) {
        let dataset = [];
        for (let resultIdx = 0; resultIdx < data.length; resultIdx++) {
            let dataPoints = [];
            let results = data[resultIdx]['results'];
            for (let i = 0; i < results.length; i++) {
                let cost = results[i]['cost'];
                let executionTime = results[i]['execution-time'] >= 0 ? results[i]['execution-time'] : results[i]['execution-time-in-seconds'];
                dataPoints.push([cost, executionTime]);
            }
            dataset.push({
                label: data[resultIdx]['start-time'],
                clickable: true,
                hoverable: true,
                data: dataPoints
            });
        }
        $scope.dataset = dataset;
        console.log(dataset);
    };

    $scope.selectSolution = function(event, pos, item) {
        if (item) {
            $scope.solution = {
                cost: $scope.results[item.seriesIndex]['results'][item.dataIndex]['cost'],
                executionTime: $scope.results[item.seriesIndex]['results'][item.dataIndex]['execution-time'] >= 0 ? $scope.results[item.seriesIndex]['results'][item.dataIndex]['execution-time'] : $scope.results[item.seriesIndex]['results'][item.dataIndex]['execution-time-in-seconds'],
                architecture: $scope.results[item.seriesIndex]['results'][item.dataIndex]['architecture']
            }
            $scope.showSolution = true;
        } else {
            $scope.showSolution = false;
        }
    };
});
