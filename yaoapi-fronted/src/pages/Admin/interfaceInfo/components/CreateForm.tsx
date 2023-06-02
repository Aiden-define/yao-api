import {ProColumns, ProTable} from '@ant-design/pro-components';
import React from 'react';
import {Modal} from "antd";


export type Props = {
  columns: ProColumns<API.InterfaceInfo>[];
  onCancel: () => void,
  onSubmit: (values: API.InterfaceInfo) => Promise<void>;
  visible: boolean;
};

const CreateForm: React.FC<Props> = (props) => {
  const { visible,columns,onCancel,onSubmit} = props;
  return <Modal visible={visible} onCancel={()=>onCancel?.()}>
    <ProTable type="form" columns={columns} onSubmit={async (value) => {
         onSubmit?.(value)}
    }/>
  </Modal>;
};

export default CreateForm;
