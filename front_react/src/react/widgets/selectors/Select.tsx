import React, {type ReactElement, type ReactNode} from 'react'

interface Props extends React.DetailedHTMLProps<React.SelectHTMLAttributes<HTMLSelectElement>, HTMLSelectElement> {
    children?: ReactNode
    options: Array<{ id: string; option: ReactNode }>
}

const Select: React.FC<Props> = ({ children, options, className, ...rest }): ReactElement =>
    <div className="relative inline-block">
        {children}
        <select
            className={children
                ? `absolute inset-0 w-full h-full opacity-0 cursor-pointer ${className || ''}`
                : `$block w-auto px-4 py-2 bg-white border border-gray-300 rounded shadow focus:outline-none focus:ring-2 focus:ring-blue-500 ${className || ''}`
            }
            {...rest}>
        {options.map((e) => (
            <option key={e.id} value={e.id}>
                {e.option}
            </option>
        ))}
    </select>
    </div >

export default Select
