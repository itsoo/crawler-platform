var getFilterModule = function(require, exports, module) {
	var getFilter = function(field, op, value) {
		var filter = {};
		filter.columnName = field;
		filter.op = op;
		filter.value = value;
		return filter;
	};
	return {
		OP : {
			Bigger : 1,
			Equal : 2,
			Smaller : 3,
			Like : 4,
			BiggerEqual : 5,
			SmallerEqual : 6,
			NotEqual : 7,
			In : 8,
			And : 9,
			NotLike : 10,
			Null : 11,
			Between : 12,
			NotIn : 13
		},
		getFilter : function(key, value) {
			return getFilter(key, 2, value);
		},
		getFilterWithOp : function(key, value, op) {
			return getFilter(key, op, value);
		}
	};
};