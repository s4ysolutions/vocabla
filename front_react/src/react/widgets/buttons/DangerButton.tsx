import React, { ReactElement } from 'react'
import Button from './Button'

interface Props extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    title?: string
}

const DangerButton: React.FC<Props> = ({ children, title, className, ...rest }): ReactElement =>
    <Button className={`bg-red-500 text-white hover:bg-red-600 ${className || ''}`} {...rest}>
        {title}
        {children}
    </Button>

export default DangerButton