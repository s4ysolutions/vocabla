import React, {type ReactElement } from 'react'
import Button from './Button'

interface Props extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    title?: string
}

const CancelButton: React.FC<Props> = ({ children, title, className, ...rest }): ReactElement =>
    <Button className={`bg-gray-200 text-gray-700 hover:bg-gray-300 ${className || ''}`} {...rest}>
        {title}
        {children}
    </Button>

export default CancelButton
