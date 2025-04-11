import React, { ChangeEventHandler, ReactElement, ReactNode } from 'react'

interface Props {
    values: Array<{ id: string; value: string }>
    value?: string
    defaultValue?: string
    onChange?: ChangeEventHandler<HTMLSelectElement>
    children?: ReactNode
}

const Selector: React.FC<Props> = ({
    values,
    value,
    defaultValue,
    children,
    onChange, }): ReactElement =>
    <div className="relative inline-block">
        {children}
        <select
            defaultValue={defaultValue}
            value={value}
            onChange={onChange}
            className={children
                ? "absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                : "block w-auto px-4 py-2 bg-white border border-gray-300 rounded shadow focus:outline-none focus:ring-2 focus:ring-blue-500"}
        >
            {values.map((e) => (
                <option key={e.id} value={e.id}>
                    {e.value}
                </option>
            ))}
        </select>
    </div>

export default Selector