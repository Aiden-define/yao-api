import type {ProColumns, ProFormInstance} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Modal } from 'antd';
import React, { useEffect, useRef} from 'react';

export type Props = {
    columns:ProColumns<API.UserVO>[];
    values:API.UserVO;
    onCancel: () => void;
    onSubmit: (values: API.UserVO) => Promise<void>;
    open: boolean;
};

const UpdateModal: React.FC<Props> = (props) => {
    const { columns,values, open, onCancel, onSubmit } = props;

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
