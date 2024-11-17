import React, { ReactElement, ReactNode } from 'react'

interface Props {
    title?: string
    children?: ReactNode
    onClick?: () => void
}

const PrimaryButton: React.FC<Props> = ({ children, title, onClick }): ReactElement =>
    <button className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600" onClick={onClick}>
        {title}
        {children}
    </button>

export default PrimaryButton