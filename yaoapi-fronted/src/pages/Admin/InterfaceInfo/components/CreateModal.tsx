import type { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal} from 'antd';
import React from 'react';

export type Props = {
  onCancel: () => void;
  onSubmit: (values: API.InterfaceInfoAddRequest) => Promise<void>;
  open: boolean;
};
const columns: ProColumns<API.InterfaceInfoAddRequest>[] = [

  {
    title: '名称',
    dataIndex: 'name',
    tip: '接口的名称',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },
  {
    title: '描述',
    dataIndex: 'description',
    valueType: 'textarea',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },

  {
    title: '请求参数(示例)',
    dataIndex: 'requestParams',
    valueType: 'text',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },
  {
    title: '请求头',
    dataIndex: 'requestHeader',
    valueType: 'text',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },
  {
    title: '响应头',
    dataIndex: 'responseHeader',
    valueType: 'text',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },
  {
    title: '请求方法',
    dataIndex: 'method',
    valueType: 'text',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },
  {
    title: 'url',
    dataIndex: 'url',
    valueType: 'text',
    formItemProps: {
      rules: [{
        required: true,
      }]
    }
  },

];

const CreateModal: React.FC<Props> = (props) => {
  const { open, onCancel, onSubmit } = props;

  return (
    <Modal open={open} footer={null} onCancel={() => onCancel?.()}>
      <ProTable
        type="form"
        columns={columns}
        onSubmit={async (value) => {
          onSubmit?.(value);
        }}
      />
    </Modal>
  );
};
export default CreateModal;
