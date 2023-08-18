import type {ProColumns, ProFormInstance} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal } from 'antd';
import React, { useEffect, useRef} from 'react';

export type Props = {
    values: API.InterfaceInfo;
    onCancel: () => void;
    onSubmit: (values: API.InterfaceInfoUpdateRequest) => Promise<void>;
    open: boolean;
};
const columns: ProColumns<API.InterfaceInfoUpdateRequest>[] = [
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
  },
  {
    title: '请求头',
    dataIndex: 'requestHeader',
    sorter: true,
    hideInForm: true,
    valueType: 'text'
  },
  {
    title: '请求参数(示例)',
    dataIndex: 'requestParams',
    valueType: 'text',
  },
  {
    title: '响应头',
    dataIndex: 'responseHeader',
    hideInForm: true,
    valueType: 'text'
  },
  {
    title: '请求方法',
    dataIndex: 'method',
    valueType: 'text',
  },
  {
    title: 'url',
    dataIndex: 'url',
    valueType: 'text',
  },
];

const UpdateModal: React.FC<Props> = (props) => {
    const { values, open, onCancel, onSubmit } = props;

    const formRef = useRef<ProFormInstance>();

    useEffect(() => {
        if (formRef) {
            formRef.current?.setFieldsValue(values);
        }
    }, [values])

    return (
        <Modal open={open} footer={null} onCancel={() => onCancel?.()}>
            <ProTable
                type="form"
                formRef={formRef}
                columns={columns}
                onSubmit={async (value) => {
                    onSubmit?.(value);
                }}
            />
        </Modal>
    );
};
export default UpdateModal;
