<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <!-- 查询区域 -->
        <div class="table-page-search-wrapper">
          <!-- 搜索区域 -->
          <a-form layout="inline" @keyup.enter.native="searchQuery">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="名称" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入名称查询" v-model="queryParam.supplierName"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="信用代码" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入信用代码查询" v-model="queryParam.creditCode"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="入驻时间" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-range-picker
                    style="width:100%"
                    v-model="queryParam.entryTimeRange"
                    format="YYYY-MM-DD"
                    :placeholder="['开始时间', '结束时间']"
                    @change="onDateChange"
                    @ok="onDateOk"
                  />
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="状态" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-select placeholder="请选择状态" allow-clear v-model="queryParam.status">
                    <a-select-option value="0">待审核</a-select-option>
                    <a-select-option value="1">已生效</a-select-option>
                    <a-select-option value="2">已拒绝</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                <a-col :md="6" :sm="24">
                  <a-button type="primary" @click="searchQuery">查询</a-button>
                  <a-button style="margin-left: 8px" @click="searchReset">重置</a-button>
                </a-col>
              </span>
            </a-row>
          </a-form>
        </div>
        <!-- 操作按钮区域 -->
        <div class="table-operator"  style="margin-top: 5px">
          <a-button v-if="btnEnableList.indexOf(1)>-1" @click="handleAdd" type="primary" icon="plus">新增</a-button>
          <a-button v-if="btnEnableList.indexOf(1)>-1" icon="delete" @click="batchDel">删除</a-button>
          <a-button v-if="btnEnableList.indexOf(2)>-1" icon="check" @click="batchSetStatus('1')">审核通过</a-button>
          <a-button v-if="btnEnableList.indexOf(2)>-1" icon="close" @click="batchSetStatus('2')">审核拒绝</a-button>
          <a-button v-if="btnEnableList.indexOf(3)>-1" icon="download" @click="handleExportXls('供应商管理信息')">导出</a-button>
        </div>
        <!-- table区域-begin -->
        <div>
          <a-table
            ref="table"
            size="middle"
            bordered
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :pagination="ipagination"
            :scroll="scroll"
            :loading="loading"
            :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
            @change="handleTableChange">
            <span slot="action" slot-scope="text, record">
              <a @click="handleEdit(record)">编辑</a>
              <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
              <a-popconfirm v-if="btnEnableList.indexOf(1)>-1" title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                <a>删除</a>
              </a-popconfirm>
            </span>
            <!-- 状态渲染模板 -->
            <template slot="customRenderStatus" slot-scope="status">
              <a-tag v-if="status == '0'" color="orange">待审核</a-tag>
              <a-tag v-if="status == '1'" color="green">已生效</a-tag>
              <a-tag v-if="status == '2'" color="red">已拒绝</a-tag>
            </template>
            <!-- 营业执照图片渲染模板 -->
            <template slot="customRenderImage" slot-scope="value">
              <a v-if="value" @click="handleViewImage(value)">
                <img :src="getImgView(value)" style="height:40px;max-width:100px;cursor:pointer;" />
              </a>
              <span v-else>-</span>
            </template>
          </a-table>
        </div>
        <!-- table区域-end -->
        <!-- 表单区域 -->
        <supplier-manage-modal ref="modalForm" @ok="modalFormOk"></supplier-manage-modal>
        <!-- 图片预览弹窗 -->
        <a-modal :visible="previewVisible" :footer="null" @cancel="handlePreviewCancel">
          <img alt="营业执照" style="width: 100%" :src="previewImage"/>
        </a-modal>
      </a-card>
    </a-col>
  </a-row>
</template>
<script>
  import SupplierManageModal from './modules/SupplierManageModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import JDate from '@/components/jeecg/JDate'
  import Vue from 'vue'
  export default {
    name: "SupplierManageList",
    mixins:[JeecgListMixin],
    components: {
      SupplierManageModal,
      JDate
    },
    data () {
      return {
        labelCol: {
          span: 5
        },
        wrapperCol: {
          span: 18,
          offset: 1
        },
        // 查询条件
        queryParam: {
          supplierName:'',
          creditCode:'',
          beginTime:'',
          endTime:'',
          status:''
        },
        urlPath: '/system/supplierManage',
        ipagination:{
          pageSizeOptions: ['10', '20', '30', '100', '200']
        },
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
          },
          {
            title: '操作',
            dataIndex: 'action',
            width: 130,
            align:"center",
            scopedSlots: { customRender: 'action' },
          },
          { title: '名称',dataIndex: 'supplierName',width:180,align:"left"},
          { title: '信用代码', dataIndex: 'creditCode',width:180,align:"left"},
          { title: '注册地址', dataIndex: 'registerAddress',width:200,align:"left", ellipsis:true},
          { title: '经营地址', dataIndex: 'businessAddress',width:200,align:"left", ellipsis:true},
          { title: '法人', dataIndex: 'legalPerson',width:80,align:"left"},
          { title: '联系电话', dataIndex: 'contactPhone',width:120,align:"left"},
          { title: '注册资本', dataIndex: 'registeredCapital',width:100,align:"left"},
          { title: '资质', dataIndex: 'qualification',width:150,align:"left", ellipsis:true},
          { title: '营业执照', dataIndex: 'businessLicense',width:120,align:"center",
            scopedSlots: { customRender: 'customRenderImage' }
          },
          { title: '入驻时间', dataIndex: 'entryTime',width:120,align:"left"},
          { title: '状态',dataIndex: 'status',width:80,align:"center",
            scopedSlots: { customRender: 'customRenderStatus' }
          },
          { title: '创建时间',dataIndex: 'createTime',width:150,align:"left"}
        ],
        url: {
          list: "/supplierManage/list",
          delete: "/supplierManage/delete",
          deleteBatch: "/supplierManage/deleteBatch",
          exportXlsUrl: "/supplierManage/exportExcel",
          batchSetStatusUrl: "/supplierManage/batchSetStatus"
        },
        previewVisible: false,
        previewImage: ''
      }
    },
    computed: {
    },
    created() {
    },
    methods: {
      searchReset() {
        this.queryParam = {
          supplierName:'',
          creditCode:'',
          beginTime:'',
          endTime:'',
          status:''
        }
        this.loadData(1);
      },
      onDateChange(dates, dateStrings) {
        if (dateStrings && dateStrings.length === 2) {
          this.queryParam.beginTime = dateStrings[0];
          this.queryParam.endTime = dateStrings[1];
        } else {
          this.queryParam.beginTime = '';
          this.queryParam.endTime = '';
        }
      },
      onDateOk(value) {
        this.onDateChange(value, [value[0].format('YYYY-MM-DD'), value[1].format('YYYY-MM-DD')]);
      },
      handleEdit: function (record) {
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title = "编辑";
        this.$refs.modalForm.disableSubmit = false;
        if(this.btnEnableList.indexOf(1)===-1) {
          this.$refs.modalForm.isReadOnly = true
        }
      },
      handleViewImage: function(value) {
        if(value) {
          this.previewImage = this.getImgView(value);
          this.previewVisible = true;
        }
      },
      handlePreviewCancel: function() {
        this.previewVisible = false;
        this.previewImage = '';
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
