import type { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal} from 'antd';
import React from 'react';

export type Props = {
  //columns: ProColumns<API.InterfaceInfoAddRequest>[];
  onCancel: () => void;
  onSubmit: (values: API.UserAddRequest) => Promise<void>;
  open: boolean;
};
const columns: ProColumns<API.UserAddRequest>[] = [
  {
    title: '账号',
    dataIndex: 'userAccount',
    valueType: 'text',
    formItemProps: {
      rules: [
        {
          required: true,
          pattern: /^.{4,16}$/,
          message: '账号至少4位且少于16位！',
        },
      ],
    },
  },
  {
    title: '密码',
    dataIndex: 'userPassword',
    valueType: 'text',
    formItemProps: {
      rules: [
        {
          required: true,
          pattern: /^.{8,16}$/,
          message: '密码至少8位且少于16位',
        },
      ],
    },
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
