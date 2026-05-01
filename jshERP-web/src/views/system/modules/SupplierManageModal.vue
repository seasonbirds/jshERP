<template>
  <div ref="container">
    <a-modal
      :title="title"
      :width="1200"
      :visible="visible"
      :confirmLoading="confirmLoading"
      :getContainer="() => $refs.container"
      :maskStyle="{'top':'93px','left':'154px'}"
      :wrapClassName="wrapClassNameInfo()"
      :mask="isDesktop()"
      :maskClosable="false"
      @ok="handleOk"
      @cancel="handleCancel"
      cancelText="取消"
      okText="保存"
      style="top:10%;height: 80%;">
      <template slot="footer">
        <a-button key="back" v-if="isReadOnly" @click="handleCancel">
          取消
        </a-button>
        <a-button v-if="!isReadOnly && model.id && model.status == '0'" key="check" type="primary" style="margin-right: 8px;" @click="handleOkAndCheck">保存并审核通过</a-button>
        <a-button v-if="!isReadOnly" key="submit" type="primary" :loading="confirmLoading" @click="handleOk">保存</a-button>
        <a-button key="back" @click="handleCancel">取消</a-button>
      </template>
      <a-spin :spinning="confirmLoading">
        <a-form :form="form" id="supplierManageModal">
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="名称">
                <a-input placeholder="请输入名称" v-decorator.trim="[ 'supplierName', validatorRules.supplierName]" />
              </a-form-item>
            </a-col>
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="信用代码">
                <a-input placeholder="请输入信用代码" v-decorator.trim="[ 'creditCode' ]" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="注册地址">
                <a-input placeholder="请输入注册地址" v-decorator.trim="[ 'registerAddress' ]" />
              </a-form-item>
            </a-col>
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="经营地址">
                <a-input placeholder="请输入经营地址" v-decorator.trim="[ 'businessAddress' ]" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="法人">
                <a-input placeholder="请输入法人" v-decorator.trim="[ 'legalPerson' ]" />
              </a-form-item>
            </a-col>
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="联系电话">
                <a-input placeholder="请输入联系电话" v-decorator.trim="[ 'contactPhone' ]" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="注册资本">
                <a-input placeholder="请输入注册资本" v-decorator.trim="[ 'registeredCapital' ]" />
              </a-form-item>
            </a-col>
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="入驻时间">
                <j-date v-decorator="['entryTime', validatorRules.entryTime]" :show-time="false"/>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="资质">
                <a-textarea :rows="2" placeholder="请输入资质信息" v-decorator.trim="[ 'qualification' ]" />
              </a-form-item>
            </a-col>
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="状态">
                <a-select v-decorator="[ 'status' ]" :disabled="isReadOnly">
                  <a-select-option value="0">待审核</a-select-option>
                  <a-select-option value="1">已生效</a-select-option>
                  <a-select-option value="2">已拒绝</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row class="form-row" :gutter="24" style="padding-top:20px">
            <a-col :lg="18" :md="18" :sm="24">
              <a-form-item :labelCol="{xs: { span: 24 },sm: { span: 3 }}" :wrapperCol="{xs: { span: 24 },sm: { span: 20 }}" label="营业执照">
                <j-image-upload v-model="businessLicense" bizPath="supplier" text="上传"></j-image-upload>
              </a-form-item>
            </a-col>
            <a-col :lg="6" :md="6" :sm="24"></a-col>
          </a-row>
          <a-row class="form-row" :gutter="24">
            <a-col :span="24/2">
              <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="上传提示">
                单张图片大小不超过系统配置限制
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-spin>
    </a-modal>
  </div>
</template>
<script>
  import pick from 'lodash.pick'
  import { addSupplierManage, editSupplierManage, checkSupplierManageName } from '@/api/api'
  import { autoJumpNextInput } from "@/utils/util"
  import { mixinDevice } from '@/utils/mixin'
  import JImageUpload from '@/components/jeecg/JImageUpload'
  import JDate from '@/components/jeecg/JDate'

  export default {
    name: "SupplierManageModal",
    mixins: [mixinDevice],
    components: {
      JImageUpload,
      JDate
    },
    data () {
      return {
        title:"操作",
        visible: false,
        model: {},
        isReadOnly: false,
        businessLicense: '',
        labelCol: {
          xs: { span: 24 },
          sm: { span: 4 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 20 },
        },
        confirmLoading: false,
        form: this.$form.createForm(this),
        validatorRules:{
          supplierName:{
            rules: [
              { required: true, message: '请输入名称!' },
              { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
              { validator: this.validateSupplierName }
            ]
          },
          entryTime:{
            rules: [
              { required: true, message: '请选择入驻时间!' }
            ]
          }
        },
      }
    },
    created () {
    },
    methods: {
      add () {
        this.edit({});
      },
      edit (record) {
        this.form.resetFields();
        this.model = Object.assign({}, record);
        this.visible = true;
        this.businessLicense = record.businessLicense || '';
        this.$nextTick(() => {
          let status = this.model.status || '0';
          this.form.setFieldsValue(pick(this.model, 'supplierName', 'creditCode', 'registerAddress', 
            'businessAddress', 'legalPerson', 'contactPhone', 'registeredCapital', 
            'qualification', 'entryTime', 'status'))
          if (!this.model.id) {
            this.form.setFieldsValue({ 'status': '0' });
          }
          autoJumpNextInput('supplierManageModal')
        });
      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        this.submitForm(false);
      },
      handleOkAndCheck () {
        this.submitForm(true);
      },
      submitForm (isCheck) {
        const that = this;
        this.form.validateFields((err, values) => {
          if (!err) {
            that.confirmLoading = true;
            let formData = Object.assign(this.model, values);
            formData.businessLicense = this.businessLicense;
            if (isCheck) {
              formData.status = '1';
            }
            let obj;
            if(!this.model.id){
              obj=addSupplierManage(formData);
            }else{
              obj=editSupplierManage(formData);
            }
            obj.then((res)=>{
              if(res.code === 200){
                that.$emit('ok');
              }else{
                that.$message.warning(res.data.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
              that.close();
            })
          }
        })
      },
      handleCancel () {
        this.close()
      },
      validateSupplierName(rule, value, callback){
        let params = {
          supplierName: value,
          id: this.model.id?this.model.id:0
        };
        checkSupplierManageName(params).then((res)=>{
          if(res && res.code===200) {
            if(!res.data.status){
              callback();
            } else {
              callback("名称已经存在");
            }
          } else {
            callback(res.data);
          }
        });
      }
    }
  }
</script>
<style scoped>

</style>
