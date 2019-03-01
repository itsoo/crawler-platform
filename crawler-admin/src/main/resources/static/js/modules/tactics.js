var filterModule = getFilterModule();

$(function() {
	// 初始化数据
	getTactics();
});

// 采集策略页面相关
function getTactics() {
	$("#tactics-li").addClass("active");
	$("#task-li").removeClass("active");

	var $main = $("#main");
	$main.html('');
	var $addBtn = $('<button class="layui-btn layui-btn-radius add-btn"><i class="layui-icon">新增&nbsp;&nbsp;&nbsp;&#xe61f;</i></button>');
	$main.append($addBtn);
	var $tacticsTable = $('<table id="tastic" class="layui-table" lay-filter="tastic"></table>');
	$main.append($tacticsTable);

	layui.use('table', function() {
		var table = layui.table;

		// 第一个实例
		var tableTactice = table.render({
			elem : '#tastic',
			id : 'test',
			height : 600,
			url : '/managePlatform/tactics/search', // 数据接口
			where : {
				item : ''
			},
			request : {
				pageName : 'pageNo',
				limitName : 'pageSize'
			},
			page : true,
			cols : [ [// 表头
			{
				field : 'tacticsId',
				title : 'ID',
				width : 100,
				align : "center"
			}, {
				field : 'tacticsName',
				title : '策略名称',
				width : 220
			}, {
				field : 'createTime',
				title : '创建时间',
				width : 177
			}, {
				field : 'describe',
				title : '描述信息'
			}, {
				field : 'status',
				title : '状态',
				width : 60,
				align : "center"
			}, {
				width : 160,
				align : 'center',
				toolbar : '#tacticsBar'
			} ] ],
			limits : [ 10, 20, 30 ]
		});

		table.on('tool(tastic)', function(obj) {
			var $self = $(this);

			var data = obj.data; // 获得当前行数据
			var layEvent = obj.event; // 获得 lay-event 对应的值（也可以是表头的 event
			// 参数对应的值）
			var tr = obj.tr; // 获得当前行 tr 的DOM对象

			if (layEvent === 'detail') { // 查看
				var $content = getTacticsDetailTab(data);
				layer.open({
					type : 1,
					title : '采集策略详情',
					area : [ '900px', '600px' ],
					content : $content,
					cancel : function(index, layero) {
						layer.close(index);
						$("#detailDiv").remove();
					}
				});
			} else if (layEvent === 'del') { // 删除
				layer.confirm('真的删除行么', function(index) {
					deleteTactics(data.tacticsId, function() {
						obj.del(); // 删除对应行（tr）的DOM结构，并更新缓存
					}, function(msg) {
						layer.msg(msg);
					});
					layer.close(index);
				});
			} else if (layEvent === 'edit') { // 编辑
				var status = 0;
				var statusShow = '启用';
				if (data.status == 0) {
					status = 1;
					statusShow = '禁用';
				}
				changeTacticsStatus(data.tacticsId, status, function() {
					obj.update({
						status : status,
					});
					$self.html(statusShow);
				}, function(msg) {
					layer.msg(msg);
				});
			}
		});

		$addBtn.click(function() {
			getCreateTacticsTab(tableTactice);
		});
	});

}

function getTacticsDetailTab(data) {
	$tacTicsTab = $('<div id="detailDiv" class="layui-form create-tab"></div>');
	$('body').append($tacTicsTab);

	// 构造“策略名称”的输入框
	var $itemDiv1 = $('<div class="layui-form-item"></div>');
	var $label1 = $('<label class="layui-form-label create-label">采集策略名称：</label>');
	$itemDiv1.append($label1);
	var $inputDiv1 = $('<div class="layui-input-block"></div>');
	var $input1 = $('<input type="text" name="tacticsName" class="layui-input create-input" disabled></input>');
	$input1.val(data.tacticsName);
	$inputDiv1.append($input1);
	$itemDiv1.append($inputDiv1);
	$tacTicsTab.append($itemDiv1);

	// 构造“自定义策略”的代码路径输入框
	var $itemDiv2 = $('<div class="layui-form-item"></div>');
	var $label2 = $('<label class="layui-form-label create-label">代码路经：</label>');
	$itemDiv2.append($label2);
	var $inputDiv2 = $('<div class="layui-input-block"></div>');
	var $input2 = $('<input type="text" name="jarPath" class="layui-input create-input" disabled></input>');
	$input2.val(data.jarPath);
	$inputDiv2.append($input2);
	$itemDiv2.append($inputDiv2);
	$tacTicsTab.append($itemDiv2);

	// 构造“自定义策略”的处理类的全类名
	var $itemDiv3 = $('<div class="layui-form-item"></div>');
	var $label3 = $('<label class="layui-form-label create-label">类名：</label>');
	$itemDiv3.append($label3);
	var $inputDiv3 = $('<div class="layui-input-block"></div>');
	var $input3 = $('<input type="text" name="className" class="layui-input create-input" disabled></input>');
	$input3.val(data.className);
	$inputDiv3.append($input3);
	$itemDiv3.append($inputDiv3);
	$tacTicsTab.append($itemDiv3);

	// 构造“自定义策略”的描述信息
	var $itemDiv4 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label4 = $('<label class="layui-form-label create-label">描述信息：</label>');
	$itemDiv4.append($label4);
	var $inputDiv4 = $('<div class="layui-input-block"></div>');
	var $input4 = $('<textarea name="describe" class="layui-textarea create-input" disabled></textarea>');
	$input4.val(data.describe);
	$inputDiv4.append($input4);
	$itemDiv4.append($inputDiv4);
	$tacTicsTab.append($itemDiv4);

	// 构造“自定义策略”下属的任务的ID
	var $itemDiv5 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label5 = $('<label class="layui-form-label create-label">任务ID：</label>');
	$itemDiv5.append($label5);
	var $inputDiv5 = $('<div class="layui-input-block"></div>');
	var $input5 = $('<textarea name="describe" class="layui-textarea create-input" disabled></textarea>');
	getTaskIdsByTacticsId(data.tacticsId, function(taskIds) {
		$input5.val(taskIds);
	});
	$inputDiv5.append($input5);
	$itemDiv5.append($inputDiv5);
	$tacTicsTab.append($itemDiv5);

	return $tacTicsTab;
}

function getCreateTacticsTab(tableTactice) {
	var $tacTicsTab = $('<div id="createTacticeDiv" class="layui-form create-tab"></div>');
	$('body').append($tacTicsTab);

	// 构造“策略名称”的输入框
	var $itemDiv1 = $('<div class="layui-form-item"></div>');
	var $label1 = $('<label class="layui-form-label create-label">采集策略名称：</label>');
	$itemDiv1.append($label1);
	var $inputDiv1 = $('<div class="layui-input-block"></div>');
	var $input1 = $('<input type="text" name="tacticsName" placeholder="请输入采集策略的名称" autocomplete="off" class="layui-input create-input" lay-verify="required"></input>');
	$inputDiv1.append($input1);
	$itemDiv1.append($inputDiv1);
	$tacTicsTab.append($itemDiv1);

	// 构造“自定义策略”的代码路径输入框
	var $itemDiv2 = $('<div class="layui-form-item"></div>');
	var $label2 = $('<label class="layui-form-label create-label">代码路经：</label>');
	$itemDiv2.append($label2);
	var $inputDiv2 = $('<div class="layui-input-block"></div>');
	var $input2 = $('<input type="text" name="jarPath" placeholder="请输入代码路经" autocomplete="off" class="layui-input create-input" lay-verify="required"></input>');
	$inputDiv2.append($input2);
	$itemDiv2.append($inputDiv2);
	$tacTicsTab.append($itemDiv2);

	// 构造“自定义策略”的处理类的全类名
	var $itemDiv3 = $('<div class="layui-form-item"></div>');
	var $label3 = $('<label class="layui-form-label create-label">类名：</label>');
	$itemDiv3.append($label3);
	var $inputDiv3 = $('<div class="layui-input-block"></div>');
	var $input3 = $('<input type="text" name="className" placeholder="请输入类名" autocomplete="off" class="layui-input create-input" lay-verify="required"></input>');
	$inputDiv3.append($input3);
	$itemDiv3.append($inputDiv3);
	$tacTicsTab.append($itemDiv3);

	// 构造“自定义策略”的描述信息
	var $itemDiv4 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label4 = $('<label class="layui-form-label create-label">描述信息：</label>');
	$itemDiv4.append($label4);
	var $inputDiv4 = $('<div class="layui-input-block"></div>');
	var $input4 = $('<textarea name="describe" placeholder="请输入采集策略的描述信息" autocomplete="off" class="layui-textarea create-input"></textarea>');
	$inputDiv4.append($input4);
	$itemDiv4.append($inputDiv4);
	$tacTicsTab.append($itemDiv4);

	// 构造表单的提交按钮
	var $itemDiv5 = $('<div class="layui-form-item"></div>');
	var $submitDiv = $('<div class="layui-input-block"></div>');
	var $button1 = $('<button class="layui-btn" lay-submit lay-filter="createTactice">提交</button>');
	$submitDiv.append($button1);
	$itemDiv5.append($submitDiv);
	$tacTicsTab.append($itemDiv5);

	var index = layer.open({
		type : 1,
		title : '添加采集策略',
		area : [ '900px', '600px' ],
		content : $tacTicsTab,
		cancel : function(index, layero) {
			layer.close(index);
			$("#createTacticeDiv").remove();
		}
	});

	layui.use('form', function() {
		var form = layui.form;

		// 监听提交
		form.on('submit(createTactice)', function(data) {
			layer.confirm('确定添加采集策略吗？', function(index1) {
				var newTactics = data.field;
				createTactics(newTactics, function() {
					tableTactice.reload({
						where : {
							item : ''
						},
						page : {
							curr : 1
						}
					});
					layer.close(index);
					$("#createTacticeDiv").remove();
				}, function(msg) {
					layer.msg(msg);
				});

				layer.close(index1);
			});
		});
	});
}

function searchTactics(success, faild) {
	$.ajax({
		url : '/managePlatform/tactics/searchOptions',
		success : function(data) {
			if (data.code == 0) {
				success(data.data);
			} else {
				faild(data.msg);
			}
		}
	});
}

function createTactics(newTactics, success, faild) {
	$.ajax({
		url : '/managePlatform/tactics/add',
		data : {
			item : JSON.stringify(newTactics)
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

// 修改采集策略的状态
function changeTacticsStatus(tacticsId, status, success, faild) {
	$.ajax({
		url : '/managePlatform/tactics/update',
		data : {
			tacticsId : tacticsId,
			status : status
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

// 删除采集策略
function deleteTactics(tacticsId, success, faild) {
	$.ajax({
		url : '/managePlatform/tactics/delete',
		data : {
			tacticsId : tacticsId
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

function getTaskIdsByTacticsId(tacticsId, success) {
	$.ajax({
		url : '/managePlatform/tactics/getTaskIds',
		data : {
			tacticsId : tacticsId
		},
		success : function(data) {
			if (data.code == 0) {
				success(data.data);
			}
		}
	});
}

// 采集任务页面相关
function getTask() {
	$("#task-li").addClass("active");
	$("#tactics-li").removeClass("active");

	var $main = $("#main");
	$main.html('');
	var $addBtn = $('<button class="layui-btn layui-btn-radius add-btn"><i class="layui-icon">新增&nbsp;&nbsp;&nbsp;&#xe61f;</i></button>');
	$main.append($addBtn);
	var $searchDiv = $('<div class="layui-input-block div-search"></div>');
	var $searchInput = $('<input type="text" name="taskNameSearch" placeholder="输入任务名称" autocomplete="off" class="layui-input float-left input-search"></input>');
	var $searchBtn = $('<button class="layui-btn layui-btn-radius float-left"><i class="layui-icon">&#xe615;</i></button>');
	var $advancedSearchBtn = $('<button class="layui-btn layui-btn-radius float-left" style="margin-left:30px;">高级搜索</button>');
	$searchDiv.append($searchInput);
	$searchDiv.append($searchBtn);
	$searchDiv.append($advancedSearchBtn);
	$main.append($searchDiv);

	var $taskTable = $('<table id="task" class="layui-table" lay-filter="task"></table>');
	$main.append($taskTable);

	layui.use('table', function() {
		var table = layui.table;

		// 第一个实例
		var taskTable = table.render({
			elem : '#task',
			height : 600,
			url : '/managePlatform/task/search', // 数据接口
			where : {
				item : ''
			},
			request : {
				pageName : 'pageNo',
				limitName : 'pageSize'
			},
			page : true,
			cols : [ [ // 表头
			{
				field : 'taskId',
				title : 'ID',
				width : 100,
				align : "center"
			}, {
				field : 'taskName',
				title : '任务名称',
				width : 220
			}, {
				field : 'taskType',
				title : '类型',
				width : 60,
				align : "center"
			}, {
				field : 'circulation',
				title : '循环',
				width : 60,
				align : "center"
			}, {
				field : 'createTime',
				title : '创建时间',
				width : 177
			}, {
				field : 'describe',
				title : '反馈信息'
			}, {
				field : 'status',
				title : '状态',
				width : 60,
				align : "center"
			}, {
				width : 230,
				align : 'center',
				toolbar : '#taskBar'
			} ] ],
			limits : [ 10, 20, 30 ]
		});

		table.on('tool(task)', function(obj) {
			var $self = $(this);

			var data = obj.data; // 获得当前行数据
			var layEvent = obj.event; // 获得 lay-event 对应的值（也可以是表头的 event
			// 参数对应的值）
			var tr = obj.tr; // 获得当前行 tr 的DOM对象

			if (layEvent === 'del') { // 删除
				layer.confirm('真的删除行么', function(index) {
					deleteTask(data.taskId, function() {
						obj.del(); // 删除对应行（tr）的DOM结构，并更新缓存
						layer.close(index);
					});
				});
			} else if (layEvent === 'edit') { // 编辑
				getTaskDetail(data.taskId, function(detail) {
					searchTactics(function(tacticsOptions) {
						getTaskDetailUpdateTag(data, detail, tacticsOptions);
					}, function(msg) {
						layer.msg(msg);
						getTaskDetailUpdateTag(data, detail, null);
					});
				}, function(msg, taskId) {
					searchTactics(function(tacticsOptions) {
						getTaskDetailUpdateTag(data, null, tacticsOptions);
					}, function(msg1) {
						layer.msg(msg1);
						getTaskDetailUpdateTag(data, null, null);
					});
				});
			} else if (layEvent === 'start') { // 开始
				if (data.status == 1 || data.status == 2) {
					layer.msg("任务已处于开启状态");
					return;
				}
				startTask(data, function(newStatus) {
					obj.update({
						status : newStatus,
					});
				}, function(msg) {
					layer.msg(msg);
				});
			} else if (layEvent === 'stop') { // 停止
				if (data.status == 0 || data.status == 3 || data.status == 4) {
					layer.msg("任务已处于关闭状态");
					return;
				}
				stopTask(data, function(newStatus) {
					obj.update({
						status : newStatus,
					});
				}, function(msg) {
					layer.msg(msg);
				});
			}
		});

		// 绑定“新增”按钮的点击事件
		$addBtn.click(function() {
			getCreateTaskTab(taskTable);
		});

		// 绑定“搜索”按钮的点击事件
		$searchBtn.click(function() {
			var filters = [];
			var taskName = $("input[name='taskNameSearch']").val();
			var filter = filterModule.getFilterWithOp('task_name', taskName,
					filterModule.OP.Like);
			filters.push(filter);

			taskTable.reload({
				where : {
					item : JSON.stringify(filters)
				},
				page : {
					curr : 1
				}
			});
		});

		// 绑定“高级搜索”按钮的点击事件
		$advancedSearchBtn.click(function() {
			getAdvancedSearchBTab(taskTable);
		});
	});
}

function getCreateTaskTab(taskTable) {
	var $taskTab = $('<div id="createTaskDiv" class="layui-form create-tab"></div>');
	$('body').append($taskTab);

	// 构造“采集任务名称”的输入框
	var $itemDiv1 = $('<div class="layui-form-item"></div>');
	var $label1 = $('<label class="layui-form-label create-label">采集任务名称：</label>');
	$itemDiv1.append($label1);
	var $inputDiv1 = $('<div class="layui-input-block"></div>');
	var $input1 = $('<input type="text" name="taskName" placeholder="请输入采集任务的名称" autocomplete="off" class="layui-input create-input" lay-verify="required"></input>');
	$inputDiv1.append($input1);
	$itemDiv1.append($inputDiv1);
	$taskTab.append($itemDiv1);

	// 构造“采集任务”的类型字段
	var $itemDiv2 = $('<div class="layui-form-item"></div>');
	var $label2 = $('<label class="layui-form-label create-label">任务类型：</label>');
	$itemDiv2.append($label2);
	var $inputDiv2 = $('<div class="layui-input-block"></div>');
	var $input21 = $('<input type="radio" name="taskType" value="0" title="单页" checked></input>');
	var $input22 = $('<input type="radio" name="taskType" value="1" title="多页"></input>');
	$inputDiv2.append($input21);
	$inputDiv2.append($input22);
	$itemDiv2.append($inputDiv2);
	$taskTab.append($itemDiv2);

	// 构造“采集任务”的循环字段
	var $itemDiv3 = $('<div class="layui-form-item"></div>');
	var $label3 = $('<label class="layui-form-label create-label">是否循环：</label>');
	$itemDiv3.append($label3);
	var $inputDiv3 = $('<div class="layui-input-block"></div>');
	var $input31 = $('<input type="radio" name="circulation" value="0" title="否" checked></input>');
	var $input32 = $('<input type="radio" name="circulation" value="1" title="是"></input>');
	$inputDiv3.append($input31);
	$inputDiv3.append($input32);
	$itemDiv3.append($inputDiv3);
	$taskTab.append($itemDiv3);

	// 构造表单的提交按钮
	var $itemDiv4 = $('<div class="layui-form-item"></div>');
	var $submitDiv = $('<div class="layui-input-block"></div>');
	var $button = $('<button class="layui-btn" lay-submit lay-filter="createTask">提交</button>');
	$submitDiv.append($button);
	$itemDiv4.append($submitDiv);
	$taskTab.append($itemDiv4);

	var index = layer.open({
		type : 1,
		title : '添加采集任务',
		area : [ '900px', '600px' ],
		content : $taskTab,
		cancel : function(index, layero) {
			layer.close(index);
			$("#createTaskDiv").remove();
		}
	});

	layui.use('form', function() {
		var form = layui.form;
		form.render();

		// 监听提交
		form.on('submit(createTask)', function(data) {
			layer.confirm('确定添加采集任务吗？', function(index1) {
				var newTask = data.field;
				createTask(newTask, function() {
					taskTable.reload({
						where : {
							item : ''
						},
						page : {
							curr : 1
						}
					});
					layer.close(index);
					$("#createTaskDiv").remove();
				}, function(msg) {
					layer.msg(msg);
				});

				layer.close(index1);
			});
		});
	});
}

function getAdvancedSearchBTab(taskTable) {
	var $advancedSearchTab = $('<div id="advancedSearchDiv" class="layui-form create-tab"></div>');
	$('body').append($advancedSearchTab);

	// 构造“采集任务”的名称输入框
	var $itemDiv1 = $('<div class="layui-form-item"></div>');
	var $label1 = $('<label class="layui-form-label create-label">采集任务名称：</label>');
	$itemDiv1.append($label1);
	var $inputDiv1 = $('<div class="layui-input-block"></div>');
	var $input1 = $('<input type="text" name="taskNameAdSearch" placeholder="请输入采集任务的名称" autocomplete="off" class="layui-input create-input"></input>');
	$inputDiv1.append($input1);
	$itemDiv1.append($inputDiv1);
	$advancedSearchTab.append($itemDiv1);

	// 构造“采集任务”的类型字段
	var $itemDiv2 = $('<div class="layui-form-item"></div>');
	var $label2 = $('<label class="layui-form-label create-label">任务类型：</label>');
	$itemDiv2.append($label2);
	var $inputDiv2 = $('<div class="layui-input-block"></div>');
	var $input21 = $('<input type="radio" name="taskTypeAdSearch" value="0" title="单页"></input>');
	var $input22 = $('<input type="radio" name="taskTypeAdSearch" value="1" title="多页"></input>');
	var $input23 = $('<input type="radio" name="taskTypeAdSearch" value="" title="全部" checked></input>');
	$inputDiv2.append($input21);
	$inputDiv2.append($input22);
	$inputDiv2.append($input23);
	$itemDiv2.append($inputDiv2);
	$advancedSearchTab.append($itemDiv2);

	// 构造“采集任务”的循环字段
	var $itemDiv3 = $('<div class="layui-form-item"></div>');
	var $label3 = $('<label class="layui-form-label create-label">是否循环：</label>');
	$itemDiv3.append($label3);
	var $inputDiv3 = $('<div class="layui-input-block"></div>');
	var $input31 = $('<input type="radio" name="circulationAdSearch" value="0" title="否"></input>');
	var $input32 = $('<input type="radio" name="circulationAdSearch" value="1" title="是"></input>');
	var $input33 = $('<input type="radio" name="circulationAdSearch" value="" title="全部 " checked></input>');
	$inputDiv3.append($input31);
	$inputDiv3.append($input32);
	$inputDiv3.append($input33);
	$itemDiv3.append($inputDiv3);
	$advancedSearchTab.append($itemDiv3);

	// 构造“采集任务”的状态
	var $itemDivst = $('<div class="layui-form-item"></div>');
	var $labelst = $('<label class="layui-form-label create-label">任务状态：</label>');
	$itemDivst.append($labelst);
	var $inputDivst = $('<div class="layui-input-block"></div>');
	var $inputst = $('<input type="text" name="statusAdSearch" placeholder="请输入任务状态码，多个之间用英文逗号(,)隔开" autocomplete="off" class="layui-input create-input"></input>');
	$inputDivst.append($inputst);
	$itemDivst.append($inputDivst);
	$advancedSearchTab.append($itemDivst);

	// 构造表单的提交按钮
	var $itemDiv5 = $('<div class="layui-form-item"></div>');
	var $submitDiv = $('<div class="layui-input-block"></div>');
	var $button = $('<button class="layui-btn" lay-submit lay-filter="taskAdSearch">确定</button>');
	$submitDiv.append($button);
	$itemDiv5.append($submitDiv);
	$advancedSearchTab.append($itemDiv5);

	var index = layer.open({
		type : 1,
		title : '高级搜索条件',
		area : [ '900px', '600px' ],
		content : $advancedSearchTab,
		cancel : function(index, layero) {
			layer.close(index);
			$("#advancedSearchDiv").remove();
		}
	});

	var filters = [];
	layui.use('form', function() {
		var form = layui.form;
		form.render();

		// 监听提交
		form.on('submit(taskAdSearch)', function(data) {
			var taskSearch = data.field;

			$.each(taskSearch, function(key, value) {
				if (value.trim() != "") {
					if (key == "taskNameAdSearch") {
						var filter = filterModule.getFilterWithOp("task_name",
								value, filterModule.OP.Like)
						filters.push(filter);
					} else if (key == "taskTypeAdSearch") {
						var filter = filterModule.getFilterWithOp("task_type",
								value, filterModule.OP.Equal)
						filters.push(filter);

					} else if (key == "circulationAdSearch") {
						var filter = filterModule.getFilterWithOp(
								"circulation", value, filterModule.OP.Equal)
						filters.push(filter);
					} else if (key == "statusAdSearch") {
						var filter = filterModule.getFilterWithOp("status",
								value, filterModule.OP.In)
						filters.push(filter);
					}
				}
			});
			console.log(filters);

			taskTable.reload({
				where : {
					item : JSON.stringify(filters)
				},
				page : {
					curr : 1
				}
			});

			layer.close(index);
			$("#advancedSearchDiv").remove();
			$("input[name='taskNameSearch']").val('')
		});
	});
}

function createTask(newTask, success, faild) {
	$.ajax({
		url : '/managePlatform/task/add',
		data : {
			item : JSON.stringify(newTask)
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

// 获取采集任务的详情
function getTaskDetail(taskId, success, faild) {
	$.ajax({
		url : '/managePlatform/task/detail/search',
		data : {
			taskId : taskId
		},
		success : function(data) {
			if (data.code == 0) {
				success(data.data);
			} else {
				faild(data.msg, taskId);
			}
		}
	});
}

// 获取采集任务详情的修改页
function getTaskDetailUpdateTag(task, detail, tacticsOptions) {
	var $taskUpdateTab = $('<div id="taskUpdateDiv" class="layui-form create-tab"></div>');
	$('body').append($taskUpdateTab);

	// 构造“采集任务”的名称字段
	var $itemDiv1 = $('<div class="layui-form-item"></div>');
	var $label1 = $('<label class="layui-form-label create-label">任务名称：</label>');
	$itemDiv1.append($label1);
	var $inputDiv1 = $('<div class="layui-input-block"></div>');
	var $input1 = $('<input type="text" name="taskName" class="layui-input create-input" disabled></input>');
	$input1.val(task.taskName);
	$inputDiv1.append($input1);
	$itemDiv1.append($inputDiv1);
	$taskUpdateTab.append($itemDiv1);

	// 构造“采集任务”的类型字段
	var $itemDiv2 = $('<div class="layui-form-item"></div>');
	var $label2 = $('<label class="layui-form-label create-label">任务类型：</label>');
	$itemDiv2.append($label2);
	var $inputDiv2 = $('<div class="layui-input-block"></div>');
	var $input21 = $('<input type="radio" name="taskType" value="0" title="单页" disabled></input>');
	var $input22 = $('<input type="radio" name="taskType" value="1" title="多页" disabled></input>');
	if (task.taskType == 0) {
		$input21.prop("checked", true);
	} else {
		$input22.prop("checked", true);
	}
	$inputDiv2.append($input21);
	$inputDiv2.append($input22);
	$itemDiv2.append($inputDiv2);
	$taskUpdateTab.append($itemDiv2);

	// 构造“采集任务”的循环字段
	var $itemDiv3 = $('<div class="layui-form-item"></div>');
	var $label3 = $('<label class="layui-form-label create-label">是否循环：</label>');
	$itemDiv3.append($label3);
	var $inputDiv3 = $('<div class="layui-input-block"></div>');
	var $input31 = $('<input type="radio" name="circulation" value="0" title="否" disabled></input>');
	var $input32 = $('<input type="radio" name="circulation" value="1" title="是" disabled></input>');
	if (task.circulation == 0) {
		$input31.prop("checked", true);
	} else {
		$input32.prop("checked", true);
	}
	$inputDiv3.append($input31);
	$inputDiv3.append($input32);
	$itemDiv3.append($inputDiv3);
	$taskUpdateTab.append($itemDiv3);

	// 构造“采集任务”的策略选择字段
	var $itemDiv4 = $('<div class="layui-form-item"></div>');
	var $label4 = $('<label class="layui-form-label create-label">选择采集策略：</label>');
	$itemDiv4.append($label4);
	var $inputDiv4 = $('<div class="layui-inline" style="width: 300px;"></div>');
	var $select = $('<select name="tacticsId" lay-verify="required" lay-search></select>');
	var $firstOption = $('<option value="">请选择一个采集策略</option>');
	$select.append($firstOption);
	$inputDiv4.append($select);
	$itemDiv4.append($inputDiv4);
	// 从后台读取全部的可用采集策略
	if (tacticsOptions != null) {
		$.each(tacticsOptions, function() {
			var self = this;
			var $option = $('<option></option>');
			$option.val(self.tacticsId);
			$option.text(self.tacticsName);
			if (self.status == 0) {
				$option.prop("disabled", true);
			}
			$select.append($option);
			if (detail != null) {
				if (self.tacticsId == detail.tacticsId) {
					$option.prop("selected", true);
				}
			}
		});
	}
	$taskUpdateTab.append($itemDiv4);

	// 构造“采集任务”的编码字段
	var $itemDiv5 = $('<div class="layui-form-item"></div>');
	var $label5 = $('<label class="layui-form-label create-label">网页编码：</label>');
	$itemDiv5.append($label5);
	var $inputDiv5 = $('<div class="layui-input-block"></div>');
	var $input5 = $('<input type="text" name="charset" placeholder="请输入网页的编码" autocomplete="off" class="layui-input create-input-mini" lay-verify="required"></input>');
	if (detail != null) {
		$input5.val(detail.charset);
	}
	$inputDiv5.append($input5);
	$itemDiv5.append($inputDiv5);
	$taskUpdateTab.append($itemDiv5);

	// 构造“采集任务”的网址字段
	var $itemDiv6 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label6 = $('<label class="layui-form-label create-label">网址：</label>');
	$itemDiv6.append($label6);
	var $inputDiv6 = $('<div class="layui-input-block"></div>');
	var $input6 = $('<textarea name="urlPath" placeholder="请输入待采集的网址" autocomplete="off" class="layui-textarea create-input" lay-verify="required"></textarea>');
	if (detail != null) {
		$input6.val(detail.urlPath);
	}
	$inputDiv6.append($input6);
	$itemDiv6.append($inputDiv6);
	$taskUpdateTab.append($itemDiv6);

	// 构造“采集任务”的网址参数字段
	var $itemDiv7 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label7 = $('<label class="layui-form-label create-label">网址参数：</label>');
	$itemDiv7.append($label7);
	var $inputDiv7 = $('<div class="layui-input-block"></div>');
	var $input7 = $('<textarea name="urlParams" placeholder="请输入网址参数，这是一个二维数组" autocomplete="off" class="layui-textarea create-input"></textarea>');
	if (detail != null) {
		$input7.val(detail.urlParams);
	}
	$inputDiv7.append($input7);
	$itemDiv7.append($inputDiv7);
	$taskUpdateTab.append($itemDiv7);

	// 构造“采集任务”的网址参数字段
	var $itemDivHeader = $('<div class="layui-form-item layui-form-text"></div>');
	var $labelHeader = $('<label class="layui-form-label create-label">消息头：</label>');
	$itemDivHeader.append($labelHeader);
	var $inputDivHeader = $('<div class="layui-input-block"></div>');
	var $inputHeader = $('<textarea name="requestHeader" placeholder="请输入消息头" autocomplete="off" class="layui-textarea create-input"></textarea>');
	if (detail != null) {
		$inputHeader.val(detail.requestHeader);
	}
	$inputDivHeader.append($inputHeader);
	$itemDivHeader.append($inputDivHeader);
	$taskUpdateTab.append($itemDivHeader);

	// 构造“采集任务”的开始和结束时间
	var $itemDiv8 = $('<div class="layui-form-item layui-form-text"></div>');
	var $inlineDiv = $('<div class="layui-inline"></div>');
	var $label8 = $('<label class="layui-form-label create-label">时间范围：</label>');
	$inlineDiv.append($label8);
	$itemDiv8.append($inlineDiv);
	var $inputDiv81 = $('<div class="layui-input-inline" style="width: 150px;"></div>');
	var $input81 = $('<input type="text" name="startTime" autocomplete="off" class="layui-input">');
	if (detail != null) {
		$input81.val(detail.startTime);
	}
	$inputDiv81.append($input81);
	$inlineDiv.append($inputDiv81);
	var $midDiv = $('<div class="layui-form-mid">--</div>');
	$inlineDiv.append($midDiv);
	var $inputDiv82 = $('<div class="layui-input-inline" style="width: 150px;"></div>');
	var $input82 = $('<input type="text" name="endTime" autocomplete="off" class="layui-input">');
	if (detail != null) {
		$input82.val(detail.endTime);
	}
	$inputDiv82.append($input82);
	$inlineDiv.append($inputDiv82);
	$taskUpdateTab.append($itemDiv8);

	// 构造“采集任务”的采集间隔
	var $itemDivsl = $('<div class="layui-form-item"></div>');
	var $labelsl = $('<label class="layui-form-label create-label">采集间隔(ms)：</label>');
	$itemDivsl.append($labelsl);
	var $inputDivsl = $('<div class="layui-input-block"></div>');
	var $inputsl = $('<input type="text" name="sleepTime" placeholder="网页的爬取间隔，防止反爬" autocomplete="off" class="layui-input create-input-mini" lay-verify="required"></input>');
	if (detail != null) {
		$inputsl.val(detail.sleepTime);
	}
	$inputDivsl.append($inputsl);
	$itemDivsl.append($inputDivsl);
	$taskUpdateTab.append($itemDivsl);

	// 构造“采集任务”的循环字段
	var $itemDiv9 = $('<div class="layui-form-item"></div>');
	var $label9 = $('<label class="layui-form-label create-label">解析类型：</label>');
	$itemDiv9.append($label9);
	var $inputDiv9 = $('<div class="layui-input-block"></div>');
	var $input91 = $('<input type="radio" name="parseType" value="0" title="Xpath" lay-filter="parseTypeRadio"></input>');
	var $input92 = $('<input type="radio" name="parseType" value="1" title="JS" lay-filter="parseTypeRadio"></input>');
	if (detail != null) {
		if (detail.parseType == 0) {
			$input91.prop("checked", true);
		} else {
			$input92.prop("checked", true);
		}
	} else {
		$input91.prop("checked", true);
	}
	var $demoBtn = $('<button class="layui-btn layui-btn-radius layui-btn-danger  layui-btn-sm add-btn">demo</button>');
	$inputDiv9.append($input91);
	$inputDiv9.append($input92);
	$inputDiv9.append($demoBtn);
	$itemDiv9.append($inputDiv9);
	$taskUpdateTab.append($itemDiv9);

	// 构造用来显示demo的textarea
	var $itemDivDemo = $('<div class="layui-form-item layui-form-text"></div>');
	var $labelDemo = $('<label class="layui-form-label create-label" style="color:#F00"><strong>详情demo：</strong></label>');
	$itemDivDemo.append($labelDemo);
	var $inputDivDemo = $('<div class="layui-input-block"></div>');
	var $inputDemoXpath = $('#xpathCode').clone();
	var $inputDemoJS = $('#jsCode').clone();
	$inputDivDemo.append($inputDemoXpath);
	$inputDivDemo.append($inputDemoJS);
	$itemDivDemo.append($inputDivDemo);
	$taskUpdateTab.append($itemDivDemo);
	$itemDivDemo.toggle();
	if ($input91.prop("checked")) {
		$inputDemoXpath.css("display", "");
	} else if ($input92.prop("checked")) {
		$inputDemoJS.css("display", "");
	}

	// 构造“采集任务”的解析详情字段
	var $itemDiv10 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label10 = $('<label class="layui-form-label create-label">解析详情：</label>');
	$itemDiv10.append($label10);
	var $inputDiv10 = $('<div class="layui-input-block"></div>');
	var $input10 = $('<textarea name="parseDetail" placeholder="请根据选择的解析类型来配置对应的解析详情，可参照demo" autocomplete="off" class="layui-textarea create-input" lay-verify="required"></textarea>');
	if (detail != null) {
		$input10.val(detail.parseDetail);
	}
	$inputDiv10.append($input10);
	$itemDiv10.append($inputDiv10);
	$taskUpdateTab.append($itemDiv10);

	// 构造“采集任务”的采集进度字段
	var $itemDiv11 = $('<div class="layui-form-item layui-form-text"></div>');
	var $label11 = $('<label class="layui-form-label create-label">采集进度：</label>');
	$itemDiv11.append($label11);
	var $inputDiv11 = $('<div class="layui-input-block"></div>');
	var $input11 = $('<textarea name="stopFloor" autocomplete="off" class="layui-textarea create-input"></textarea>');
	if (detail != null) {
		$input11.val(detail.stopFloor);
	}
	$inputDiv11.append($input11);
	$itemDiv11.append($inputDiv11);
	$taskUpdateTab.append($itemDiv11);

	// 构造表单的提交按钮
	var $itemDiv15 = $('<div class="layui-form-item"></div>');
	var $submitDiv = $('<div class="layui-input-block"></div>');
	var $button = $('<button class="layui-btn" lay-submit lay-filter="updateTaskDetail">提交</button>');
	$submitDiv.append($button);
	$itemDiv15.append($submitDiv);
	$taskUpdateTab.append($itemDiv15);

	var index = layer.open({
		type : 1,
		title : '编辑任务详情',
		area : [ '900px', '600px' ],
		content : $taskUpdateTab,
		cancel : function(index1, layero) {
			layer.close(index1);
			$("#taskUpdateDiv").remove();
		}
	});

	layui.use('form', function() {
		var form = layui.form;
		form.render();

		form.on('radio(parseTypeRadio)', function(data) {
			if (data.value == 0) {
				$inputDemoXpath.css("display", "");
				$inputDemoJS.css("display", "none");
			} else if (data.value == 1) {
				$inputDemoXpath.css("display", "none");
				$inputDemoJS.css("display", "");
			}
		});

		// 监听提交
		form.on('submit(updateTaskDetail)', function(data) {
			// 如果任务的状态为1：“启用”或2：“执行中”时不允许修改任务详情
			if (task.status == 1 || task.status == 2) {
				layer.msg("该任务当前状态下不可以被修改");
				return;
			}

			layer.confirm('确定修改采集任务详情吗？', function(index1) {
				layer.close(index1);

				var taskDeatil = data.field;
				taskDeatil.taskId = task.taskId;

				// 如果打开详情页时，详情detail为null，说明是新建任务没有详情，此时要添加详情
				if (detail == null) {
					addTaskDetail(taskDeatil, function() {
						layer.close(index);
						$("#taskUpdateDiv").remove();
					}, function(msg) {
						layer.msg(msg);
					});
				} else {
					taskDeatil.detailId = detail.detailId;
					changeTaskDetail(taskDeatil, function() {
						layer.close(index);
						$("#taskUpdateDiv").remove();
					}, function(msg) {
						layer.msg(msg);
					});
				}
			});
		});

	});

	$demoBtn.click(function() {
		$itemDivDemo.toggle();
	});

	return $taskUpdateTab;
}

// 删除采集任务
function deleteTask(taskId, success, faild) {
	$.ajax({
		url : '/managePlatform/task/delete',
		data : {
			taskId : taskId
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

// 启用任务
function startTask(task, success, faild) {
	$.ajax({
		url : '/managePlatform/task/start',
		data : {
			taskId : task.taskId
		},
		success : function(data) {
			if (data.code == 0) {
				success(data.data);
			} else {
				faild(data.msg);
			}
		}
	});
}

// 停用任务
function stopTask(task, success, faild) {
	$.ajax({
		url : '/managePlatform/task/stop',
		data : {
			taskId : task.taskId
		},
		success : function(data) {
			if (data.code == 0) {
				success(data.data);
			} else {
				faild(data.msg);
			}
		}
	});
}

// 修改任务详情
function changeTaskDetail(taskDetail, success, faild) {
	$.ajax({
		url : '/managePlatform/task/detail/update',
		data : {
			item : JSON.stringify(taskDetail)
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}

// 添加任务详情
function addTaskDetail(item, success, faild) {
	$.ajax({
		url : '/managePlatform/task/detail/add',
		data : {
			item : JSON.stringify(item)
		},
		success : function(data) {
			if (data.code == 0) {
				success();
			} else {
				faild(data.msg);
			}
		}
	});
}
